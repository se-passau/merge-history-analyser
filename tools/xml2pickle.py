import xml.etree.ElementTree as ET
from collections import defaultdict, OrderedDict
from sys import argv
import numpy as np
import collections
import pickle
from defaultOrderedDict import *

tree = ET.parse(argv[1])
root = tree.getroot()

testMaps = DefaultOrderedDict()

for i in range(len(root)):
    scenario = root[i]

    # REMERGE
    id = scenario[0].text
    mergestate = scenario[1].get('state')
    buildstate = scenario[2].get('state')
    tests = scenario[3]

    # print id, mergestate, buildstate
    # print len(tests)

    for test in tests:
        testMaps[id][test[0].text] = (test[1].text, 0, 0, 0)

    # Parent 1
    parent1 = scenario[4]
    parent1tests = scenario[4][2]
    # print len(parent1tests)

    for test in parent1tests:
        entry = testMaps[id].get(test[0].text)
        if (entry == None):
            # print "new parent1 test"
            testMaps[id][test[0].text] = (0, test[1].text, 0, 0)
        else:
            testMaps[id][test[0].text] = (entry[0], test[1].text, entry[2], entry[3])

    # Parent 2
    parten2 = scenario[5]
    parent2tests = scenario[5][2]
    # print len(parent2tests)

    for test in parent2tests:
        entry = testMaps[id].get(test[0].text)
        if (entry == None):
            # print "new parent2 test"
            testMaps[id][test[0].text] = (0, test[1].text, 0, 0)
        else:
            testMaps[id][test[0].text] = (entry[0], entry[1], test[1].text, entry[3])

    # Pushed
    pushed = scenario[6]
    pushedtests = scenario[6][2]
    # print len(pushedtests)

    for test in pushedtests:
        entry = testMaps[id].get(test[0].text)
        if (entry == None):
            # print "new pushed test"
            testMaps[id][test[0].text] = (0, test[1].text, 0, 0)
        else:
            testMaps[id][test[0].text] = (entry[0], entry[1], entry[2], test[1].text)

    pickle.dump(testMaps, open(argv[1].split('.')[0] + '.pickle', 'wb'))