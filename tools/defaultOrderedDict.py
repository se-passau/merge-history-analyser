from collections import OrderedDict

class DefaultOrderedDict(OrderedDict):
    def __missing__(self, key):
        self[key] = type(self)()
        return self[key]