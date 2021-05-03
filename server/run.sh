fsettings="../.idea/settings.ini"          #! path from server folder to settings file
executable="java -jar ../bin/client01.jar" #! path from server folder to temp args file

# list of player types and the corresponding start commands
function start_client() {
    case "${PLAYERS%$PNEXT}" in
    's')
        echo "[ $i S ] waiting for client..."
        touch "$fclient"
        ;;
    'i')
        echo "[ $i I ] starting interface"
        ./client_gl >"${flog}$i" 2>"${ferr}$i" &
        ;;
    'o')
        echo "[ $i O ] starting client"
        $executable >"${flog}$i" 2>"${ferr}$i" &
        ;;
    't')
        echo "[ $i T ] starting trivial client"
        ./ai_trivial >"${flog}$i" 2>"${ferr}$i" &
        ;;
    esac
}

# List of all processes that must be terminated after the game if they are still running
function kill_all() {
    pkill -f 'client_gl'
    pkill -f 'server_gl'
    pkill -f 'server_nogl'
    pkill -f 'ai_trivial'
    pkill -f "$executable"
    echo '[ RUN ] processes stopped'
    rm -f "$fclient"
}

# © JOHANNES SCHMID, REBEKKA SEIDENSCHWAND, PETER PAULUS @ G3 2021

function init() {
    [ $server_use_clear == "true" ] && clear || echo ""
    echo "[ VER ] SERVER version 1.1"
    echo "[ RUN ] waiting..."
}

function wait() {
    while true; do
        sleep .1
        if ! awk "/$1/{exit 1}" "${flog}0"; then
            break
        elif ! awk "/FAILED:/{exit 1}" "${flog}0"; then
            break
        elif ! [ -f "$fplayer" ]; then
            break
        fi
    done
}
function wait_removed() {
    while true; do
        sleep .1
        if ! [ -f "$1" ]; then
            break
        fi
    done
}

if ! [ -f "$fsettings" ]; then
    echo "[ ERR ] no settings file at $fsettings"
    echo "        create by starting client run.sh"
    exit 1
fi

server_log_path=$(awk -F "= " '/server_log_path/ {print $2}' "$fsettings")
server_ret_path=$(awk -F "= " '/server_ret_path/ {print $2}' "$fsettings")
server_use_clear=$(awk -F "= " '/server_use_clear/ {print $2}' "$fsettings")
server_show_map=$(awk -F "= " '/server_show_map/ {print $2}' "$fsettings")
server_delete_txts=$(awk -F "= " '/server_delete_txts/ {print $2}' "$fsettings")
fmap="${server_log_path}map"
fplayer="${server_log_path}player"
fclient="${server_log_path}client"
flog="${server_log_path}log"
ferr="${server_log_path}err"

trap "echo ''; echo '[ ERR ] TERMINATED'; kill_all; exit 2" SIGHUP SIGINT SIGTERM

mkdir -p $server_log_path
init

while true; do
    if [ -f "$fplayer" ]; then
        for i in {0..8}; do
            rm -f "${flog}$i"
            rm -f "${ferr}$i"
        done
        [ $server_use_clear == "true" ] && clear || echo ""
        echo "[ RUN ] map recieved"

        [ $server_show_map == "true" ] && echo "[ MAP ]"
        [ $server_show_map == "true" ] && head -n 55 "$fmap"

        read PLAYERS MODE <"$fplayer"
        echo "[ RUN ] players: $PLAYERS, mode: $MODE"
        args="-C -m $fmap"
        [ "${MODE:0:1}" == "t" ] && args+=" -t ${MODE:1}" || args+=" -d ${MODE:1}"

        echo "[ RUN ] starting server"
        cd ${server_log_path}
        ${server_ret_path}server_nogl $args >"${flog}0" 2>"${ferr}0" &
        cd ${server_ret_path}
        wait "Port number is"
        if awk "/FAILED:/{exit 1}" "${flog}0" && awk "/Port number is 7777./{exit 1}" "${flog}0"; then
            echo "[ ERR ] server started on wrong port"
            kill_all
            exit 1
        fi
        i=1
        while [ ${#PLAYERS} -gt 0 ]; do
            [ -f "$fplayer" ] && awk "/FAILED:/{exit 1}" "${flog}0" || break
            PNEXT=${PLAYERS#?}
            start_client
            wait "Waiting client $i to connect...OK"
            PLAYERS=$PNEXT
            ((i++))
        done

        if [ -f "$fplayer" ] && awk "/FAILED:/{exit 1}" "${flog}0"; then
            touch "$fclient"
            echo "[ RUN ] game running"
            wait "bye bye."
        fi
        if ! awk "/FAILED:/{exit 1}" "${flog}0"; then
            echo "[ RUN ] server failed"
            touch "$fclient"
            cat >>${ferr}0 <<<"FAILED, please check log0"
            sleep 1
        elif [ -f "$fplayer" ]; then
            echo "[ RUN ] game finished"
        else
            echo "[ RUN ] game terminated"
        fi

        rm -f "$fclient"
        wait_removed $fplayer

        kill_all
        sleep .5

        [ "$server_delete_txts" == "true" ] && rm -f ${flog}_game_*

        init
    fi
    sleep .5
done
