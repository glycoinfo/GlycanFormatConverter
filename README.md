## Requirement
* Java 8 (or later)
* maven 3.6 (or later)

## Overview
GlycanFormatConverter behaves as a library for implementing glycan text conversion functions.

If you would to convert any glycan text format at stand-alone, please refer to GlycanFormatConverter-cli.
* https://github.com/glycoinfo/GlycanFormatConverter-cli

GlycanFormatConverter is also available as an API.
* https://api.glycosmos.org/glycanformatconverter/

## Release note

### 2.6.0
WURCS
* Added an exception for deoxy substituent
    * ex. \*SO/2=O/2=O (sulfate group) cannot support in the latest edition

IUPAC-Extended
* Added exceptions:
    * pyruvate (x-, r- and s-) groups

KCF (KEGG Chemical Function)
* Added acyl group.

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

## Publication
[Tsuchiya Shinichiro, Yamada Issaku, Kiyoko F. Aoki-Kinoshita. GlycanFormatConverter: a conversion tool for translating the complexities of glycans. Bioinformatics. 2019 Jul 15;35(14):2434-2440.](https://pubmed.ncbi.nlm.nih.gov/30535258/)
