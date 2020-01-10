# -*- coding: utf-8 -*-
"""
Created on Fri Jan 10 00:38:51 2020

@author: 212613144
"""
import copy
import astor
import ast

class WhileLoopTransformation(ast.NodeTransformer):
    def visit_While(self, node):
        print(astor.to_source(node))
        l1 = copy.deepcopy(node.body)
        l1.append(ast.While(test=node.test, body=node.body, orelse = []))
        print(l1)
        l2 = copy.deepcopy(node.body)
        l2.append(ast.If(test = node.test, body = l1, orelse = []))
        print(l1)
        node = ast.copy_location(ast.If(test = node.test, body = l2, orelse = []), node)
        ast.fix_missing_locations(node)
        print(astor.to_source(node))
        return node

class codeTransform(object):
    def __init__(self, codeStr):
         self.tree = ast.parse(codeStr)
         
    def whileTransformation(self):
         tree = WhileLoopTransformation().visit(self.tree)
         return astor.to_source(tree)