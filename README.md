## Requirement
* Java 8 (or later)
* maven 3.6 (or later)

## Overview
GlycanFormatConverter behaves as a library for implementing glycan text conversion functions.

If you would to convert any glycan text format at local, please refer to GlycanFormatConverter-cli.
* https://github.com/glycoinfo/GlycanFormatConverter-cli

GlycanFormatConverter is also available as an API.
* https://api.glycosmos.org/glycanformatconverter/

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

## Citation
Please citation as below:\
Tsuchiya S, Yamada I, Aoki-Kinoshita KF. GlycanFormatConverter: a conversion tool for translating the complexities of glycans. Bioinformatics. 2019 Jul 15;35(14):2434-2440. 