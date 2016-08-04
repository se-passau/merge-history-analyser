import xml.etree.ElementTree as ET
import os
from collections import OrderedDict
from sys import argv
import matplotlib.pyplot as plt
import numpy as np
import pickle
import seaborn
from matplotlib import gridspec


testMaps = pickle.load(open(argv[1], 'rb'))

    # ------ Print test with different results ----
    # for testName in testMaps[i].keys():
    #     entry = testMaps[i].get(testName)
    #     if len(set(entry)) > 1:
    #         print testName, entry



    # -------- Compare merged with something ------
    # pnewPass = 0
    # pnewFail = 0
    #
    # for testName in testMaps[i].keys():
    #     (m, p1, p2, p) = testMaps[i].get(testName)
    #     compareWith = p
    #     if(m == 0):
    #         print 'No merge result for test ' + testName
    #     elif (compareWith == 0):
    #         print 'No compared result for test ' + testName
    #     else:
    #         if(m == 'pass' and compareWith == 'fail'):
    #             pnewFail += 1
    #         if(m == 'fail' and compareWith == 'pass'):
    #             pnewPass += 1
    #
    # print "compared new passes: " + str(pnewPass)
    # print "compared new failes: " + str(pnewFail)
    # print


def filterByMask(testMapValues, mask):
    string_mask = map(lambda x: map(lambda y: "pass" if y else "fail", x), mask)
    return map(lambda x: len(filter(lambda (m, p1, p2, p): m == x[0] and p1 == x[1] and p2 == x[2] and p == x[3], testMapValues)), string_mask)

parents_pass = OrderedDict()
one_parent_pass = OrderedDict()
parents_fail = OrderedDict()

parents_pass_percentage = OrderedDict()
one_parent_pass_percentage = OrderedDict()
parents_fail_percentage = OrderedDict()

colLabels = ("", "pushed passed", "pushed failed")
# Calc numbers and percentages
for scenarioID in testMaps.keys():
    testMapValues = testMaps[scenarioID].values()
    # rowLabels = ("merge passed", "merge failed")

    parents_pass[scenarioID] = [[0, 0], [0, 0]]
    one_parent_pass[scenarioID] = [[0, 0], [0, 0]]
    parents_fail[scenarioID] = [[0, 0], [0, 0]]

    # Get table values
    parents_pass_mask = [[True, True, True, True], [True, True, True, False], [False, True, True, True], [False, True, True, False]]
    parents_pass[scenarioID] = filterByMask(testMapValues, parents_pass_mask)

    p1_pass_mask = [[True, True, False, True], [True, True, False, False], [False, True, False, True], [False, True, False, False]]
    p2_pass_mask = [[True, False, True, True], [True, False, True, False], [False, False, True, True], [False, False, True, False]]
    one_parent_pass[scenarioID] = filterByMask(testMapValues, p1_pass_mask)
    one_parent_pass[scenarioID] = np.add(one_parent_pass[scenarioID], filterByMask(testMapValues, p2_pass_mask)).tolist()

    parents_fail_mask = [[True, False, False, True], [True, False, False, False], [False, False, False, True], [False, False, False, False]]
    parents_fail[scenarioID] = filterByMask(testMapValues, parents_fail_mask)

    # Get percentages
    parents_pass_percentage[scenarioID] = np.divide(map(float,parents_pass[scenarioID]), sum(parents_pass[scenarioID])).tolist() if sum(parents_pass[scenarioID]) != 0 else 0
    one_parent_pass_percentage[scenarioID] = np.divide(map(float, one_parent_pass[scenarioID]), sum(one_parent_pass[scenarioID])).tolist() if sum(one_parent_pass[scenarioID]) != 0 else 0
    parents_fail_percentage[scenarioID] = np.divide(map(float, parents_fail[scenarioID]), sum(parents_fail[scenarioID])).tolist() if sum(parents_fail[scenarioID]) != 0 else 0




# # ----- Average Table ------
pp_help_table = np.zeros([len(parents_pass_percentage.keys()), 4])
op_help_table = np.zeros([len(one_parent_pass_percentage.keys()), 4])
pf_help_table = np.zeros([len(parents_fail_percentage.keys()), 4])


for i in range(len(parents_pass_percentage.keys())):
    key = parents_pass_percentage.keys()[i]
    pp_average_percentage[i] = parents_pass_percentage[key]
pp_average_percentage = map(lambda x: np.mean(x), pp_average_percentage.T)
pp_average_percentage = map(lambda x: str('%.2f' % (100*x) + '%'), pp_average_percentage)

# for key in one_parent_pass_percentage.keys():
#     op_average_percentage += one_parent_pass_percentage[key]
#
# op_average_percentage /= len(one_parent_pass_percentage.keys())
# op_average_percentage = map(lambda x: str('%.2f' % (100*x) + '%'), op_average_percentage)
#
# for key in parents_fail_percentage.keys():
#     pf_average_percentage += parents_fail_percentage[key]
# pf_average_percentage /= len(parents_fail_percentage.keys())
# pf_average_percentage = map(lambda x: str('%.2f' % (100*x) + '%'), pf_average_percentage)


gs = gridspec.GridSpec(2, 2)
ax0 = plt.subplot2grid((3,2), (0,1))
ax1 = plt.subplot2grid((3,2), (1,1))
ax2 = plt.subplot2grid((3,2), (2, 1))
ax3 = plt.subplot2grid((2,3), (0, 0), rowspan=3)

# Get into table format
pp_average_percentage = np.array(pp_average_percentage).reshape(2,2).tolist()
# op_average_percentage = np.array(op_average_percentage).reshape(2,2).tolist()
# pf_average_percentage = np.array(pf_average_percentage).reshape(2,2).tolist()

# Add row labels
pp_average_percentage[0] = ["merge passed"] + pp_average_percentage[0]
pp_average_percentage[1] = ["merge fail"] + pp_average_percentage[1]
# op_average_percentage[0] = ["merge passed"] + op_average_percentage[0]
# op_average_percentage[1] = ["merge fail"] + op_average_percentage[1]
# pf_average_percentage[0] = ["merge passed"] + pf_average_percentage[0]
# pf_average_percentage[1] = ["merge fail"] + pf_average_percentage[1]

ax0.set_title('both parents pass')
ax0.table(cellText=pp_average_percentage, colLabels=colLabels, loc='center', cellLoc='center')
# ax1.set_title('one parent passes')
# ax1.table(cellText=op_average_percentage, colLabels=colLabels, loc='center', cellLoc='center')
# ax2.set_title('both parents fail')
# ax2.table(cellText=pf_average_percentage, colLabels=colLabels, loc='center', cellLoc='center')

for ax in [ax0, ax1, ax2]:
    ax.xaxis.set_visible(False)
    ax.yaxis.set_visible(False)
    ax.axis('off')
    for sp in ax.spines.itervalues():
        sp.set_color('w')
        sp.set_zorder(0)

# f.tight_layout()
plt.gcf().set_size_inches(22,9)
# plt.savefig('voldemort_averagetable2.png', dpi=300)
plt.show()




# Plot
# for scenarioID in testMaps.keys():
#     # Get into table format
#     parents_pass[scenarioID] = np.array(parents_pass[scenarioID]).reshape(2,2).tolist()
#     one_parent_pass[scenarioID] = np.array(one_parent_pass[scenarioID]).reshape(2,2).tolist()
#     parents_fail[scenarioID] = np.array(parents_fail[scenarioID]).reshape(2,2).tolist()
#
#     # Add percentages
#     for i in range(2):
#         for j in range(2):
#             parents_pass[scenarioID][i][j] = str(parents_pass[scenarioID][i][j]) + '  (' + str("%.2f" % (parents_pass_percentage[scenarioID][2*i+j] * 100) + ')%')
#             one_parent_pass[scenarioID][i][j] = str(one_parent_pass[scenarioID][i][j]) + '  (' + str("%.2f" % (one_parent_pass_percentage[scenarioID][2*i+j] * 100) + ')%')
#             parents_fail[scenarioID][i][j] = str(parents_fail[scenarioID][i][j]) + '  (' + str("%.2f" % (parents_fail_percentage[scenarioID][2*i+j] * 100) + ')%')
#
#     # Add row labels
#     parents_pass[scenarioID][0] = ["merge passed"] + parents_pass[scenarioID][0]
#     parents_pass[scenarioID][1] = ["merge fail"] + parents_pass[scenarioID][1]
#     one_parent_pass[scenarioID][0] = ["merge passed"] + one_parent_pass[scenarioID][0]
#     one_parent_pass[scenarioID][1] = ["merge fail"] + one_parent_pass[scenarioID][1]
#     parents_fail[scenarioID][0] = ["merge passed"] + parents_fail[scenarioID][0]
#     parents_fail[scenarioID][1] = ["merge fail"] + parents_fail[scenarioID][1]
#
#
#     # Print table
#     gs = gridspec.GridSpec(2, 2)
#     ax0 = plt.subplot2grid((3,2), (0,1))
#     ax1 = plt.subplot2grid((3,2), (1,1))
#     ax2 = plt.subplot2grid((3,2), (2, 1))
#     ax3 = plt.subplot2grid((2,3), (0, 0), rowspan=3)
#
#     ax0.set_title('both parents pass')
#     ax0.table(cellText=parents_pass[scenarioID], colLabels=colLabels, loc='center', cellLoc='center')
#     ax1.set_title('one parent passes')
#     ax1.table(cellText=one_parent_pass[scenarioID], colLabels=colLabels, loc='center', cellLoc='center')
#     ax2.set_title('both parents fail')
#     ax2.table(cellText=parents_fail[scenarioID], colLabels=colLabels, loc='center', cellLoc='center')
#
#     for ax in [ax0, ax1, ax2]:
#         ax.xaxis.set_visible(False)
#         ax.yaxis.set_visible(False)
#         ax.axis('off')
#         for sp in ax.spines.itervalues():
#             sp.set_color('w')
#             sp.set_zorder(0)
#
#     # f.tight_layout()
#     # plt.savefig('voldemort' + str(scenarioIndex) + '_table.png')
#     # plt.show()
#
#
#
#     testMap = testMaps[scenarioID]
#     testMapItems = sorted(testMap.items())
#     indexedTestList = zip(range(len(testMapItems)), testMapItems)
#
#     mP = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): m == 'pass', indexedTestList))
#     mF = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): m == 'fail', indexedTestList))
#     mS = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): m == 'skip', indexedTestList))
#
#     pP = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p == 'pass', indexedTestList))
#     pF = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p == 'fail', indexedTestList))
#     pS = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p == 'skip', indexedTestList))
#
#     p1P = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p1 == 'pass', indexedTestList))
#     p1F = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p1 == 'fail', indexedTestList))
#     p1S = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p1 == 'skip', indexedTestList))
#
#     p2P = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p2 == 'pass', indexedTestList))
#     p2F = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p2 == 'fail', indexedTestList))
#     p2S = map(lambda (index, entry): index, filter(lambda (index, (name, (m, p1, p2, p))): p2 == 'skip', indexedTestList))
#
#     distance = 0.01
#
#     ax3.bar(pP, np.ones([len(pP)]), 1, color = 'g', edgecolor = 'g')
#     ax3.bar(pF, np.ones([len(pF)]), 1, color = 'r', edgecolor = 'r')
#     ax3.bar(pS, np.ones([len(pS)]), 1, color = 'w', edgecolor = 'w')
#
#     ax3.bar(range(len(testMap)), [distance] * len(testMap), 1, color = 'b', bottom=[1] * len(testMap))
#
#     ax3.bar(mP, np.ones([len(mP)]), 1, color = 'g', edgecolor = 'g', bottom=[1 + distance] * len(mP))
#     ax3.bar(mF, np.ones([len(mF)]), 1, color = 'r', edgecolor = 'r', bottom=[1 + distance] * len(mF))
#     ax3.bar(mS, np.ones([len(mS)]), 1, color = 'w', edgecolor = 'w', bottom=[1 + distance] * len(mS))
#
#     ax3.bar(range(len(testMap)), [distance] * len(testMap), 1, color = 'b', bottom=[2 + distance] * len(testMap))
#
#     ax3.bar(p1P, np.ones([len(p1P)]), 1, color = 'g', edgecolor = 'g', bottom=[2 + 2*distance] * len(p1P))
#     ax3.bar(p1F, np.ones([len(p1F)]), 1, color = 'r', edgecolor = 'r', bottom=[2 + 2*distance] * len(p1F))
#     ax3.bar(p1S, np.ones([len(p1S)]), 1, color = 'w', edgecolor = 'w', bottom=[2 + 2*distance] * len(p1S))
#
#     ax3.bar(range(len(testMap)), [distance] * len(testMap), 1, color = 'b', bottom=[3 + 2*distance] * len(testMap))
#
#     ax3.bar(p2P, np.ones([len(p2P)]), 1, color = 'g', edgecolor = 'g', bottom=[3 + 3*distance] * len(p2P))
#     ax3.bar(p2F, np.ones([len(p2F)]), 1, color = 'r', edgecolor = 'r', bottom=[3 + 3*distance] * len(p2F))
#     ax3.bar(p2S, np.ones([len(p2S)]), 1, color = 'w', edgecolor = 'w', bottom=[3 + 3*distance] * len(p2S))
#
#     ax3.set_title("Compare test results of voldemorts merge \n" + scenarioID)
#     ax3.set_xlim([0,len(testMap)])
#     ax3.set_ylim([0,4])
#     ax3.set_xlabel('tests')
#     gampsize = 30
#     ax3.set_ylabel('pushed' + ' '*gampsize + 'merged' + ' '*gampsize + 'parent1' + ' '*gampsize + 'parent2')
#     ax3.yaxis.set_ticks([])
#
#     plt.gcf().set_size_inches(22,9)
#     plt.savefig('voldemort' + scenarioID + '_both.png', dpi=300)
#     # plt.show()
#     # plt.close()
