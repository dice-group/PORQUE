import sys
import os
from rdflib import Graph

graph_en = Graph()
graph_en.parse('mappingbased-objects_lang=en.ttl', format='ttl')

graph_es = Graph()
graph_es.parse('mappingbased-objects_lang=es.ttl', format='ttl')

graph_de = Graph()
graph_de.parse('mappingbased-objects_lang=de.ttl', format='ttl')

graph_fr = Graph()
graph_fr.parse('mappingbased-objects_lang=fr.ttl', format='ttl')


def graph_to_list(graph):
    subjects = []
    predicates = []
    objects = []
    for s, p, o in graph:
        subjects.append(s)
        predicates.append(p)
        objects.append(o)
    
    return(list(set(subjects)), list(set(predicates)), list(set(objects)))

subjects, predicates, objects = graph_to_list(graph_en)

with open("Subjects/subjects_en.txt", "w") as file:
    file.write(str(subjects))

with open("Predicates/predicates_en.txt", "w") as file:
    file.write(str(predicates))

with open("Objects/objects_en.txt", "w") as file:
    file.write(str(objects))

subjects, predicates, objects = graph_to_list(graph_es)

with open("Subjects/subjects_es.txt", "w") as file:
    file.write(str(subjects))

with open("Predicates/predicates_es.txt", "w") as file:
    file.write(str(predicates))

with open("Objects/objects_es.txt", "w") as file:
    file.write(str(objects))


subjects, predicates, objects = graph_to_list(graph_de)

with open("Subjects/subjects_de.txt", "w") as file:
    file.write(str(subjects))

with open("Predicates/predicates_de.txt", "w") as file:
    file.write(str(predicates))

with open("Objects/objects_de.txt", "w") as file:
    file.write(str(objects))


subjects, predicates, objects = graph_to_list(graph_fr)

with open("Subjects/subjects_fr.txt", "w") as file:
    file.write(str(subjects))

with open("Predicates/predicates_fr.txt", "w") as file:
    file.write(str(predicates))

with open("Objects/objects_fr.txt", "w") as file:
    file.write(str(objects))