#!/bin/sh
# Script to generate an arbitrary number of processes, products and emissions
# in our LCA language.

if [ "$#" -lt 1 ]; then
	printf "%s usage: %s <COMPLEXITY:UINT>\n" "$0" "$0" 1>&2
	exit 1
fi

COMPLEXITY="$1"

print_process()
{
	printf "process proc_%d {\n\tproducts {\n" "$1"
	printf "\t\t1 kg prod_%d\n\t}\n" "$1"
	if [ "$2" -gt 0 ]; then
		printf "\tinputs {\n"
		printf "\t\t1 kg prod_%d\n" "$2"
		printf "\t\t1 kg prod_%d\n" "$(($2 - 1))"
		printf "\t}\n"
	fi
	printf "}\n"
}

printf "package ch.kleis.generated.treetest\n\n"

d="$COMPLEXITY"
n=0
m=0
while [ "$d" -gt 1 ]; do
	i=$((d >> 1))
	while [ "$i" -gt 0 ]; do
		print_process $((i + n)) "$m"
		i=$((i - 1))
		m=$((m - 2))
	done
	n=$((n + (d >> 1)))
	m=$n
	d=$((d >> 1))
done
