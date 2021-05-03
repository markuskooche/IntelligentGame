fsettings="./.idea/settings.ini" #! path to settings file

# © JOHANNES SCHMID, REBEKKA SEIDENSCHWAND, PETER PAULUS @ G3 2021

function init() {
    echo "[ VER ] COMPARE version 1.1"
}

function usage() {
    init
    echo "[ ERR ] $1"
    echo "[USAGE] ./compare.sh"
    echo "        -> start compare on last played map"
    echo "[USAGE] ./compare.sh <1>"
    echo "        -> play map <1> and compare the results"
    echo "           1: path to map (./maps/...)"
    exit 1
}

if ! [ -f "$fsettings" ]; then
    echo "[ ERR ] no settings file at $fsettings"
    echo "        create by starting client run.sh"
    exit 1
fi
client_log_path=$(awk -F "= " '/client_log_path/ {print $2}' "$fsettings")
fmap="${client_log_path}map"
flog="${client_log_path}log"

if [ $# -eq 4 ]; then
    map_begin="$1"
    map_end="$2"
    map_linestart=$3

    map=0
    count=0
    while IFS= read -r line; do
        if [[ $line == *"$map_begin"* ]]; then
            echo "$count"
            ((count++))
            map=1
        elif [[ $line == *"$map_end"* ]]; then
            map=0
        elif [ $map -eq 1 ]; then
            # A="$(sed -r 's/\x1b\[[0-9;]*[m|J|H]//g' <<<$line)"
            echo "${line:$map_linestart}"
        fi
    done <"${flog}$4"
elif [ $# -eq 2 ]; then
    [ -f $1 ] || usage "path $1 doesn't exist"
    read PLAYERCOUNT <$1
    PLAYERCOUNT=$(sed 's/\r$//g' <<<$PLAYERCOUNT)
    ./run.sh $1 $(printf 'o%.0s' $(seq 1 $PLAYERCOUNT)) $2 && ./compare.sh
    exit $?
else
    init
    for i in {0..8}; do
        rm -f "${fmap}$i"
    done
    read PLAYERCOUNT <"${fmap}"
    PLAYERCOUNT=$(sed 's/\r$//g' <<<$PLAYERCOUNT)
    echo "[ CMP ] playercount: $PLAYERCOUNT"
    pl=0
    while [ $pl -lt $PLAYERCOUNT ]; do
        ((pl++))
        echo "[ CMP ] mapping $pl/$PLAYERCOUNT..."
        ./compare.sh "[MAP]" "[END]" 0 $pl >"${fmap}$pl"
    done
    sleep 1
    echo "[ CMP ] mapping server..."
    ./compare.sh "/--" "valid positions:" 6 0 >"${fmap}0"
    echo "[ CMP ] mapping done"

    pl=0
    err_count_all=0
    while [ $pl -lt $PLAYERCOUNT ]; do
        ((pl++))
        echo "[ CMP ] comparing $pl"
        re='^[0-9]+$'
        err_count_line=0
        err_count=0
        linenum=0
        fieldnum=0
        incomplete=0
        if ! [ -f "$fmap$pl" ]; then
            echo "[ ERR ] missing log file"
            continue
        fi
        while read -u 4 -r f2 && read -u 3 -r f1 || read -u 3 -r f1; do
            if ! [[ $f1 =~ $re ]]; then
                if [ $incomplete -eq 0 ] && [ "$f1" != "$f2" ]; then
                    ((err_count_line++))
                    if [ "$f2" == "" ] || [ "$f1" == "" ]; then
                        printf "[ DIF ] field %3d incomplete\n" $fieldnum
                        incomplete=1
                    else
                        printf "[ DIF ] field %3d line %2d: $f1 | $f2\n" $fieldnum $linenum
                    fi
                fi
            else
                fieldnum="$f1"
                if [ $err_count_line -ne 0 ]; then
                    ((err_count++))
                    ((err_count_all++))
                    err_count_line=0
                fi
                linenum=0
                incomplete=0
            fi
            ((linenum++))
        done 3<${fmap}0 4<$fmap$pl

        if [ $err_count_line -ne 0 ]; then
            ((err_count++))
            ((err_count_all++))
        fi

        if [ $err_count -eq 0 ]; then
            echo "[ O K ] found no differences"
        else
            [ $err_count -eq 1 ] && s="" || s="s"
            echo "[ ERR ] found $err_count difference$s"
        fi
    done
    echo "[ CMP ] comparing done"
    if [ $err_count_all -eq 0 ]; then
        echo "[ O K ] SUMMARY: found no differences"
        exit 0
    else
        [ $err_count_all -eq 1 ] && s="" || s="s"
        echo "[ ERR ] SUMMARY: found $err_count_all difference$s"
        exit 5
    fi
fi
