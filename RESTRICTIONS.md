# Restrictions
This section describes the system limitations of GlycanFormatConverter.

## Conversion (2.6.0 or later)
If you input a text of IUPAC (short, condensed or extended) format that contains the following glycan, the text will be handled as an exception and conversion error will be output.

Examples of the restrictions:
- Containing repeating units
- Containing cyclic units
- Containing cross-linked substituent
- Containing repeating units with cross-linked substituent
- Containing cyclic substituent in any monosaccharide
- Containing monosaccharide fragments
- Containing substituent fragments
- Containing monosaccharide modifications such as deoxy

## Monosaccharide (2.5.0 or later)

### WURCS
WURCS format often contains monosaccharides with partially unclear structural information.\
Such unclear information is written as x or X.  In below cases, these WURCS strings can not handle in the GlycanFormatConverter.
* case 1
  * WURCS=2.0/1,1,0/[AOOxxh]/1/
* case 2
  * WURCS=2.0/1,1,0/[hXxh_2\*C_4\*OPO/3O/3=O]/1/
* case 3
  * WURCS=2.0/1,1,0/[h21xxh]/1/

The following error message will be output.  SkeletonCode is a string that indicates a composition of monosaccharide in WURCS format.
```
This SkeletonCode partially contains the wild card (x or X).
```

## Substituent (2.6.0 or later)
### Supporting monovalent substituents
| Notation              | O-linked（H_AT_OH） | N-linked           | C-linked（DEOXY）  | 
| --------------------- | ------------------- | ------------------ | ------------------ | 
| Acyl                  | :heavy_check_mark:  |                    |                    | 
| Ethyl                 | :heavy_check_mark:  |                    |                    | 
| Methyl                | :heavy_check_mark:  | :heavy_check_mark: | :heavy_check_mark: | 
| Acethyl               | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Glycolyl              | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Sulfate               | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Formyl                | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Amidino               | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Succinate             | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Dimethyl              | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Phosphate             | :heavy_check_mark:  |                    |                    | 
| Phosphpcholine        | :heavy_check_mark:  |                    |                    | 
| Ethanol               | :heavy_check_mark:  | :heavy_check_mark: |                    | 
| Diphosphoethanolamine | :heavy_check_mark:  |                    |                    | 
| Phosphoethanolamine   | :heavy_check_mark:  |                    |                    | 
| Pyrophosphate         | :heavy_check_mark:  |                    |                    | 
| Triphosphate          | :heavy_check_mark:  |                    |                    | 
| Hydroxymethyl         | :heavy_check_mark:  |                    |                    | 
| Thio                  |                     |                    | :heavy_check_mark: | 
| Amine                 |                     |                    | :heavy_check_mark: | 
| Fluoro                |                     |                    | :heavy_check_mark: | 
| Chloro                |                     |                    | :heavy_check_mark: | 
| Bromo                 |                     |                    | :heavy_check_mark: | 
| Iodo                  |                     |                    | :heavy_check_mark: | 
| r/s-carboxymethyl     | :heavy_check_mark:  |                    |                    | 
| r/s-lactate           | :heavy_check_mark:  |                    |                    | 

### Supporting divalent substituent
| Notation              | O-linked（H_AT_OH） | N-linked           | C-linked（DEOXY）   | 
| --------------------- | ------------------ | ------------------ | ------------------ | 
| Anydro                | :heavy_check_mark: |                    |                    | 
| r/s-pyruvate          | :heavy_check_mark: |                    |                    | 
| r/s-deoxypyruvate     | :heavy_check_mark: |                    |                    | 
| Thio                  |                    |                    | :heavy_check_mark: | 
| Amino                 |                    |                    | :heavy_check_mark: | 
| Ethanolamine          |                    |                    | :heavy_check_mark: | 
| Imino                 |                    |                    | :heavy_check_mark: | 
| Sulfate               | :heavy_check_mark: | :heavy_check_mark: |                    | 
| Succinate             | :heavy_check_mark: |                    |                    | 
| Phosphate             | :heavy_check_mark: |                    |                    | 
| Pyrophosphate         | :heavy_check_mark: |                    |                    | 
| Triphosphate          | :heavy_check_mark: |                    |                    | 
| Phosphoethanoleamine  |                    |                    | :heavy_check_mark: | 
| Diphosphoethanolamine |                    |                    | :heavy_check_mark: | 

* WURCS\
MAP in WURCS indicate substituent, this notation differs depending on the state of the molecule when bound to the monosaccharide.\
For example, if WURCS string input to GlycanFormatConverter contains MAP of `*SO/2=O/2=O`, it will be output a message indicating a conversion error.\
`*SO/2=O/2=O` is indicates the state of DEOXY in the monovalent substituent.

* IUPAC-Extended\
IUPAC converter can not be handle divalent substituents such as r/s-pyruvate

* KCF (KEGG Chemical Function)\
Added acyl group