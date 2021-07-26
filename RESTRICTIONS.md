# Restrictions

## Conversion (2.6.0 or later)
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
| Notation              | O-linked (H_AT_OH) | N-linked           | C-linked (DEOXY)   | 
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

WURCS
* Added an exception for deoxy substituent
    * ex. \*SO/2=O/2=O (sulfate group bonded at monosaccharide with deoxy) cannot support in the latest version

IUPAC-Extended
* Added exceptions for pyruvate (x-, r- and s-) group

KCF (KEGG Chemical Function)
* Added acyl group