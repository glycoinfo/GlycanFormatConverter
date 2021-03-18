## Requirement
* Java 8 (or later)
* maven 3.6 (or later)

## Build
```
$ mvn clean compile assembly:single
```

* When compilation in finished, jar file is created in the target folder.
>[INFO] Building jar: /../../GlycanFormatConverter/target/glycanformatconverter.jar

## Usage
```
$ java -jar target/glycanformatconverter.jar -i <FORMAT> -e <FORMAT> -seq <SEQUENCE>
```

## Options
|Option|Argument|Description|
| ---- |  ----  |    ----   |
|-e, --export|FORMAT=<br>[IUPAC-Short\|IUPAC-Condensed\|IUPAC-Extended\|GlycoCT\|WURCS\|GlycanWeb]|export format|
|-h, --help||Show usage help|
|-i, --import|FORMAT=<br>[IUPAC-Condensed\|IUPAC-Extended\|GlycoCT\|KCF\|LinearCode\|WURCS]|import format|
|-seq, --sequence|SEQUENCE|Glycan text format|

## Example
1. WURCS to IUPAC-Extended
```
$ java -jar target/glycanformatconverter.jar -i WURCS -e IUPAC-Extended -seq WURCS=2.0/5,9,8/[a2122h-1x_1-5][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O][a1221m-1a_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-2-4-5-3-2-5/a4-b1_b3-c1_b6-g1_c4-d1_d2-e1_d6-f2_g4-h1_h6-i2
```
```
α-D-Neup5Ac-(2→6)[α-L-Fucp-(1→2)]-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→3)[α-D-Neup5Ac-(2→6)-β-D-Galp-(1→4)-β-D-GlcpNAc-(1→6)]-β-D-Galp-(1→4)-?-D-Glcp-(1→
```

2. WURCS to GlycoCT
```
$ java -jar target/glycanformatconverter.jar -i WURCS -e GlycoCT -seq WURCS=2.0/5,9,8/[a2122h-1x_1-5][a2112h-1b_1-5][a2122h-1b_1-5_2*NCC/3=O][a1221m-1a_1-5][Aad21122h-2a_2-6_5*NCC/3=O]/1-2-3-2-4-5-3-2-5/a4-b1_b3-c1_b6-g1_c4-d1_d2-e1_d6-f2_g4-h1_h6-i2
```
```
RES
1b:x-dglc-HEX-1:5
2b:b-dgal-HEX-1:5
3b:b-dglc-HEX-1:5
4s:n-acetyl
5b:b-dgal-HEX-1:5
6b:a-lgal-HEX-1:5|6:d
7b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d
8s:n-acetyl
9b:b-dglc-HEX-1:5
10s:n-acetyl
11b:b-dgal-HEX-1:5
12b:a-dgro-dgal-NON-2:6|1:a|2:keto|3:d
13s:n-acetyl
LIN
1:1o(4+1)2d
2:2o(3+1)3d
3:3d(2+1)4n
4:3o(4+1)5d
5:5o(2+1)6d
6:5o(6+2)7d
7:7d(5+1)8n
8:2o(6+1)9d
9:9d(2+1)10n
10:9o(4+1)11d
11:11o(6+2)12d
12:12d(5+1)13n
```

## Restrictions
If input the text of IUPAC (-Extended, -Condensed, -Short) format contains inappropriate structural characters, a conversion error based on the restrictions will be returned.
Such restrictions are takes into account when converting from IUPAC format to WURCS format.
An examples of the restrictions is shown below:
- Containing repeating units
- Containing cyclic units
- Containing cross-linked substituent
- Containing repeating units with cross-linked substituent
- Containing cyclic substituent in any monosaccharide
- Containing monosaccharide fragments
- Containing substituent fragments
- Containing monosaccharide modifications such as deoxy

## Anything else
This tool is released as web API.
* https://api.glycosmos.org/glycanformatconverter/
