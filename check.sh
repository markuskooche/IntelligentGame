fsettings="./.idea/settings.ini"

function init() {
    echo "[ VER ] CKECK version 1.0"
}

function usage() {
    init
    echo "[ ERR ] $1"
    echo "[USAGE] ./check.sh 1"
    echo "        -> start check on default folder with server mode 1"
    echo "[USAGE] ./check.sh 1 2"
    echo "        -> start check on folder 1 with server mode 2"
    echo "        server mode:"
    echo "          tX: timelimit X seconds"
    echo "          dX: depthlimit X layers"
    exit 1
}

function wait_removed() {
    while true; do
        sleep 1
        if ! [ -f "$1" ]; then
            break
        fi
    done
}

function check() {
    read PLAYERCOUNT <$1
    PLAYERCOUNT=$(sed 's/\r$//g' <<<$PLAYERCOUNT)

    ./run.sh $1 $(printf 'o%.0s' $(seq 1 $PLAYERCOUNT)) $mode >"${client_log_path}check$(printf "%02d" $counter)" &&
        ./compare.sh >>"${client_log_path}check$(printf "%02d" $counter)"
    case "$?" in
    0)
        echo "[ O K ] $(printf "%02d" $counter) $1"
        ;;
    1)
        echo "[ ERR ] USAGE ERROR"
        echo "        check logs for more information"
        exit 1
        ;;
    2)
        echo ""
        echo "[ ERR ] TERMINATED"
        exit 2
        ;;
    3)
        echo ""
        echo "[ ERR ] $(printf "%02d" $counter) $1"
        echo "        ERROR OCCURED"
        echo "        check logs for more information"
        exit 3
        ;;
    5)
        echo "[ ERR ] $(printf "%02d" $counter) $1"
        ;;
    esac
}

init

if ! [ -f "$fsettings" ]; then
    echo "[ ERR ] no settings file at $fsettings"
    echo "        create by starting client run.sh"
    exit 1
fi
client_log_path=$(awk -F "= " '/client_log_path/ {print $2}' "$fsettings")
check_default_folder=$(awk -F "= " '/check_default_folder/ {print $2}' "$fsettings")

[ $# -ge 1 ] || usage "invalid arguments"

echo "[CHECK] deleting old logs"
shopt -s nullglob
for file in ${client_log_path}check*; do
    rm $file
done

if [ $# -eq 1 ]; then
    echo "[CHECK] default folder"
    folder=$check_default_folder
    mode=$1
else
    echo "[CHECK] folder $1"
    folder=$1
    mode=$2
fi
[ -d $folder ] || usage "folder doesn't exist"

counter=1
shopt -s nullglob
for file in $folder*; do
    check $file $mode $counter
    # sleep 1.01
    ((counter++))
done

echo "[CHECK] folder checked"

# © JOHANNES SCHMID, REBEKKA SEIDENSCHWAND, PETER PAULUS @ G3 2021
