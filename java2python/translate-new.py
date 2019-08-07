import connexion
import json
import os
import subprocess
import uuid

from flask import request

TMPDIR='/tmp/'

if __name__ == '__main__':
    app = connexion.App(__name__, specification_dir='swagger/', debug=True)
    app.add_api('translateAPI-new.yaml')
    app.run(port=19092)

def generateUUID():
    return str(uuid.uuid4())

def createFilenames(UUID):
    infilename = TMPDIR + generateUUID() + '/inputfile.java'
    outfilename = infilename.replace('/inputfile.java', '/outputfile.py')
    os.makedirs(os.path.dirname(infilename), exist_ok=True)
    return infilename, outfilename

def translate(javaFilename, pythonFilename):

    status = True
    try:
        subprocess.check_output(['j2py -k ' + javaFilename + ' ' + pythonFilename], stderr=subprocess.STDOUT, shell=True)
    except subprocess.CalledProcessError as e:
        print("Error in translation to Python 2: {0}".format(e))
        status = False

    if status:
        try:
            subprocess.check_output(['2to3-3.7 -n -w ' + pythonFilename], stderr=subprocess.STDOUT, shell=True)
        except subprocess.CalledProcessError as e:
            print("Error in conversion from Python 2 to 3: {0}".format(e))
            status = False

    return status

def translateFile():
    file = request.files['javaFile']
    infilename, outfilename = createFilenames(generateUUID())
    file.save(infilename)
    print('Translating code')

    if translate(infilename, outfilename):
        outfile = open(outfilename)
        output = { 'status': 'SUCCESS', 'code': outfile.read() }
    else:
        output = { 'status': 'FAILURE' }
    return output #json.dumps(output)

def translateMethod(javaMethod):

    javaText = 'public class ' + javaMethod["className"] + ' { \n'
    javaText += javaMethod["methodCode"] + '\n'
    javaText += '}\n'

    infilename, outfilename = createFilenames(generateUUID())
    with open(infilename, 'wt', encoding='utf-8') as file:
        file.write(javaText)
    
    if translate(infilename, outfilename):
        outfile = open(outfilename)
        output = { 'status': 'SUCCESS', 'code': outfile.read() }
    else:
        output = { 'status': 'FAILURE' }
    return output #json.dumps(output)

def translateExpression(javaExpr):

    javaExprStr = javaExpr['exprCode']
    if javaExprStr.endswith('}') or javaExprStr.endswith(';'):
        pass
    else:
        javaExprStr += ';'	# temporary hack. Introduce more meaningful check later
    javaText = 'public class ' + javaExpr['className'] + ' { \n'
    javaText += '\t public void ' + javaExpr['methodName'] + '(){ \n'
    javaText += javaExprStr + '\n'
    javaText += '\t }\n'
    javaText += '}\n'

    infilename, outfilename = createFilenames(generateUUID())
    with open(infilename, 'wt', encoding='utf-8') as file:
        file.write(javaText)

    if translate(infilename, outfilename):
        outfile = open(outfilename)
        output = { 'status': 'SUCCESS', 'code': outfile.read() }
    else:
        output = { 'status': 'FAILURE' }
    return output #json.dumps(output)

