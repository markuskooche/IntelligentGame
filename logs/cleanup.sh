fsettings="../.idea/settings.ini"

function init() {
    echo "[ VER ] CLEANUP version 1.0"
}

function usage() {
    init
    echo "[ ERR ] $1"
    echo "[USAGE] ./cleanup.sh"
    echo "        -> start cleanup the last played map"
    exit 1
}

if ! [ -f "$fsettings" ]; then
    echo "[ ERR ] no settings file at $fsettings"
    echo "        create by starting client run.sh"
    exit 1
fi
group="XT$(awk -F "= " '/group_id/ {print $2}' "$fsettings")-"

if [ $# -eq 2 ]; then
    cleanup_player=$1
    file="./log$cleanup_player"

    mkdir -p $2
    
    while read line; do
        if [[ $line = $group* ]]; then
            echo $line >> $2/analyse$1.log
        fi
    done < $file

    for f in "./"*; do
        if [[ $f = ./err* ]] || [[ $f = ./map* ]]; then
            cp $f $2
        elif [[ $f = ./log* ]]; then
            if [[ $f != *.txt ]]; then
                cp $f $2
            fi
        fi
    done
else
    usage
fi

# © MARKUS KOCH, IWAN ECKERT, BENEDIKT HALBRITTER @ G1 2021
