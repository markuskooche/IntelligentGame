#!/usr/bin/env bash

# delete old files
echo "Previously compiled files will be deleted..."
rm -f Projektbericht.log
rm -f Projektbericht.aux
rm -f Projektbericht.out
rm -f Projektbericht.pdf
rm -f Projektbericht.toc
rm -f Projektbericht.bbl
rm -f Projektbericht.blg
echo "Previously compiled files was deleted."


# create new files
if [[ $1 != "-d" ]]; then
    echo ""
    echo "The report is compiled..."
    {
        pdflatex Projektbericht.tex
        bibtex Projektbericht.aux
        pdflatex Projektbericht.tex
        pdflatex Projektbericht.tex
    } &> /dev/null
    echo "FINISHED"
fi
