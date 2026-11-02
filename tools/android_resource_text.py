"""Decode Android resource quote delimiters and escapes for source extraction."""
import re

def decode(text):
    text=text or ''
    result=[];index=0
    escapes={"n":"\n","r":"\r","t":"\t"}
    while index<len(text):
        char=text[index]
        if char=='"':
            index+=1;continue
        if char=="\\" and index+1<len(text):
            index+=1;char=text[index]
            if char=='u' and re.fullmatch(r'[0-9a-fA-F]{4}',text[index+1:index+5]):
                result.append(chr(int(text[index+1:index+5],16)));index+=5;continue
            result.append(escapes.get(char,char))
        else:result.append(char)
        index+=1
    return ''.join(result)
