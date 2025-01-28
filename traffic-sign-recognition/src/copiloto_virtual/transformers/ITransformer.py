# create an interface for the transformer

from abc import ABC, abstractmethod

class ITransformer(ABC):

    def __init__(self):
        pass

    @abstractmethod

    def transform(self):
        pass
