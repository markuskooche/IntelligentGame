fsettings="./.idea/settings.ini"
fargs="./.idea/args"

function make_cl() {
    make gradle build
}
function start_cl() {
    java -jar ./bin/client01.jar -i "$client_ip" -p "$client_port" $additional_args
}
types_allowed="[s|i|o|t]"

function init() {
    [ client_use_clear == "true" ] && clear
    echo "[ VER ] CLIENT version 1.0"
}

function usage() {
    init
    echo "[ ERR ] $1"
    echo "        usage: ./run.sh 1 2 3 (4)"
    echo "        1:  path to map (./maps/...)"
    echo "        2:  player type order (1-8):"
    echo "              s: self: this client"
    echo "              o: other: server launched client"
    echo "              i: interface: interface to play yourself"
    echo "              t: trivial: server launched trivial client"
    echo "        3:  server mode:"
    echo "              tX: timelimit X seconds"
    echo "              dX: depthlimit X layers"
    echo "        4+: additional arguments for self client (optional)"
    echo "              needs to be in \" "
    exit 1
}

function wait_created() {
    while true; do
        sleep 1
        if [ -f "$1" ]; then
            break
        fi
    done
}
function wait_removed() {
    while true; do
        sleep 1
        if ! [ -f "$1" ]; then
            break
        fi
    done
}

if ! [ -f "$fsettings" ]; then
    echo "[ ERR ] no settings file at $fsettings"
    echo "        creating with default values"
    cat >$fsettings <<EOF
client_log_path      = ./logs/
client_ip            = 0.0.0.0
client_port          = 7777
client_use_clear     = true
client_show_map      = true

server_log_path      = ../logs/
server_ret_path      = ../server/
server_use_clear     = true
server_show_map      = true
server_delete_txts   = true

check_default_folder =./maps/evilMaps/
EOF
    exit 1
fi

client_log_path=$(awk -F "= " '/client_log_path/ {print $2}' "$fsettings")
client_ip=$(awk -F "= " '/client_ip/ {print $2}' "$fsettings")
client_port=$(awk -F "= " '/client_port/ {print $2}' "$fsettings")
client_use_clear=$(awk -F "= " '/client_use_clear/ {print $2}' "$fsettings")
client_show_map=$(awk -F "= " '/client_show_map/ {print $2}' "$fsettings")
fmap="${client_log_path}map"
fplayer="${client_log_path}player"
fclient="${client_log_path}client"
flog="${client_log_path}log"
ferr="${client_log_path}err"

trap "echo ''; echo '[ ERR ] TERMINATED'; rm -f '$fplayer'; exit 2" SIGHUP SIGINT SIGTERM

if [ -f "$fsettings" ]; then
    client_ip=$(awk -F "= " '/ip/ {print $2}' "$fsettings")
    client_port=$(awk -F "= " '/port/ {print $2}' "$fsettings")
else
    client_ip="0.0.0.0"
    client_port="7777"
fi

mkdir -p logs

if [ "$1" == "STOP" ]; then
    if [ -f "$fclient" ] || [ -f "$fplayer" ]; then
        rm -f "$fplayer"
        echo "[ RUN ] game terminating..."
        wait_removed "$fclient"
        echo "[ RUN ] game terminated"
    else
        echo "[ RUN ] game finished"
    fi
    exit
fi

iserr=0
if [ -f "$fargs" ] && [ $# -eq 0 ] || [ $# -eq 3 ] || [ $# -eq 4 ] || [ "$1" == "DEBUG" ]; then
    if [ $# -ge 3 ]; then
        arg_map="$1"
        arg_types="$2"
        arg_smode="$3"
        [ $# -eq 4 ] && additional_args="$4" || additional_args=""
        arg_save=1
        arg_debug=""
    else
        read ARGS <$fargs
        ARGS=($ARGS)
        arg_map="${ARGS[0]}"
        arg_types="${ARGS[1]}"
        arg_smode="${ARGS[2]}"
        arg_save=0
        arg_debug="$1"
        echo "[ ARG ] loaded arguments"
    fi

    [ -f "$arg_map" ] || usage "map path invalid"

    read PLAYERCOUNT <$arg_map
    PLAYERCOUNT=$(sed 's/\r$//g' <<<$PLAYERCOUNT)

    [ "${#arg_types}" -eq "$PLAYERCOUNT" ] || usage "playertypecount must be $PLAYERCOUNT"

    [[ "$arg_types" =~ ^$types_allowed+$ ]] || usage "playertypes must contain only $types_allowed"

    count="$(awk -F"s" '{print NF-1}' <<<\"${arg_types}\")"

    [ "$count" -le 1 ] || usage "playertypes can't contain more than one self"
    [ "$arg_debug" != "DEBUG" ] || [ "$count" -eq 1 ] || usage "playertypes must contain self for debugging"

    [[ "${arg_smode}" =~ ^[t|d] ]] || usage "servermode must be t or d"
    [[ "${arg_smode}" =~ ^[t|d][0-9]+$ ]] || usage "servermode must end with number"

    [ "$arg_debug" == "DEBUG" ] && client_use_clear="false"
    init

    if [ $arg_save -eq 1 ]; then
        cat >"$fargs" <<<$@
        echo "[ ARG ] saved arguments"
    fi

    echo "[BUILD] building test"
    make_cl
    if [ $? -ne 0 ]; then
        echo "[BUILD] test build failed"
        exit 1
    fi
    echo "[BUILD] test build success"

    [ "$client_show_map" == "true" ] && echo "[ MAP ]" || echo "[ MAP ] playercount: $PLAYERCOUNT"
    [ "$client_show_map" == "true" ] && cat "$arg_map"

    if [ -f "$fclient" ] || [ -f "$fplayer" ]; then
        rm -f "$fplayer"
        echo "[ RUN ] waiting for last game to stop"
        sleep 1
        wait_removed "$fclient"
    fi

    echo "[ RUN ] starting server"
    cp "$arg_map" "$fmap"
    cat >"$fplayer" <<<"$arg_types $arg_smode"

    echo "[ RUN ] waiting for server..."
    wait_created "$fclient"
    if [ "$count" -eq 1 ]; then
        if [ "$arg_debug" == "DEBUG" ]; then
            echo "[ CLI ] ready for debugger to connect"
            exit 0
        else
            echo "[ CLI ] starting ip $client_ip port $client_port"
            start_cl
        fi
        echo "[ CLI ] finished"
    else
        echo "[ RUN ] game started"
        wait_removed "$fclient"
        echo "[ RUN ] game finished"
    fi

    reg_te="Terminal state reached.$"
    reg_fi="Final state reached.$"
    reg_pl="^Player [1-8]"
    end=0
    while IFS= read -r line; do
        if [[ $line =~ $reg_te ]]; then
            echo "[ RES ] End phase 1:"
            end=1
        elif [[ $line =~ $reg_fi ]]; then
            echo "[ RES ] End phase 2:"
            end=1
        fi
        [ $end -ne 0 ] && [[ $line =~ $reg_pl ]] && echo "        $line"
    done <"${flog}0"

    ####################
    # added by group 1 #
    ####################
    NOW=`date '+%F_%H-%M-%S'`
    folder="./stored/$NOW"

    for (( i=0; i<${#arg_types}; i++ )); do
        if [[ "${arg_types:$i:1}" == o ]]; then
            our_player=$(($i+1))
            (cd logs; ./cleanup.sh $our_player $folder)
        fi
    done
    ####################

    for i in {0..8}; do
        if [ -f "${ferr}$i" ]; then
            read msg <${ferr}$i
            if [ "$msg" != "" ]; then
                echo "[ ERR ] ID $i stopped with error code"
                iserr=1
            fi
        fi
    done
else
    if [ $# -eq 1 ] && [ -f "$1" ]; then
        echo "[ MAP ] preview:"
        cat "$1"
        echo ""
    fi
    usage "invalid arguments"
fi

rm -f "$fplayer"
[ $iserr -eq 0 ] || exit 3

if [ "$arg_debug" == "DEBUG" ]; then
    exit 1
fi

# © JOHANNES SCHMID, REBEKKA SEIDENSCHWAND, PETER PAULUS @ G3 2021
