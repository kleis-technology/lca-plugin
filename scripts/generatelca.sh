#!/bin/sh
# Script to generate an arbitrary number of processes, products and emissions
# in our LCA language.

if [ "$#" -lt 1 ]; then
	printf "%s usage: %s <COMPLEXITY:UINT>\n" "$0" "$0" 1>&2
	exit 1
fi

COMPLEXITY="$1"

rand_not()
{
	RAND=$(($(dd if=/dev/random count=1 bs=1 2>/dev/null | od -An -i) % (COMPLEXITY)))
	if [ $RAND -eq "$1" ]; then
		RAND=$(rand_not $i)
	fi
	echo "$RAND"
}

printf "package ch.kleis.generated.connectedtest\n\n"

i=0
while [ ${i} -le "${COMPLEXITY}" ]; do
	PROD1=$(rand_not $i)
	PROD2=$(rand_not $i)
	PROD3=$(rand_not $i)

	printf "process proc_%d {\n\tproducts {\n\t\t1 kg prod_%d\n\t}\n" "$i" "$i"
	printf "\tinputs {\n\t\t1 kg prod_%d\n\t\t1 kg prod_%d\n\t\t1 kg prod_%d\n\t}\n}\n" \
		"$PROD1" \
		"$PROD2" \
		"$PROD3"

	i=$((i + 1))
done
