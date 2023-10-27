
with open("predicates_en.txt", "r") as file:
    predicates_en = file.read()[1:-1].split(",")

with open("predicates_de.txt", "r") as file:
    predicates_de = file.read()[1:-1].split(",")

with open("predicates_es.txt", "r") as file:
    predicates_es = file.read()[1:-1].split(",")

with open("predicates_fr.txt", "r") as file:
    predicates_fr = file.read()[1:-1].split(",")


common_predicates = 'Common predicates in EN-DE:\n'
uncommon_predicates = 'Predicts present in DE but not in EN:\n'

for predicate in predicates_de:
    if predicate in predicates_en:
        # print(predicate)
        common_predicates = common_predicates + "\n" + (predicate)
    else:
        uncommon_predicates = uncommon_predicates + "\n" + (predicate)

common_predicates = common_predicates + '\n\n\nCommon predicates in EN-ES:\n'
uncommon_predicates = uncommon_predicates + '\n\n\nPredicts present in ES but not in EN:\n'

for predicate in predicates_es:
    if predicate in predicates_en:
        # print(predicate)
        common_predicates = common_predicates + "\n" + (predicate)
    else:
        uncommon_predicates = uncommon_predicates + "\n" + (predicate)

common_predicates = common_predicates + '\n\n\nCommon predicates in EN-FR:\n'
uncommon_predicates = uncommon_predicates + '\n\n\nPredicts present in FR but not in EN:\n'

for predicate in predicates_fr:
    if predicate in predicates_en:
        # print(predicate)
        common_predicates = common_predicates + "\n" + (predicate)
    else:
        uncommon_predicates = uncommon_predicates + "\n" + (predicate)



with open("Predicate_Analysis.txt", "w") as file:
    file.write(common_predicates + "\n\n\n\n" + uncommon_predicates)
