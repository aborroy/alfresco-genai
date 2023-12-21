import os

import streamlit as st
from langchain.chains import RetrievalQA
from PyPDF2 import PdfReader
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain.callbacks.base import BaseCallbackHandler
from langchain.vectorstores.neo4j_vector import Neo4jVector
from streamlit.logger import get_logger
from chains import (
    load_embedding_model,
    load_llm,
)
from fastapi import FastAPI, UploadFile
from fastapi.middleware.cors import CORSMiddleware

from dotenv import load_dotenv

load_dotenv(".env")

url = os.getenv("NEO4J_URI")
username = os.getenv("NEO4J_USERNAME")
password = os.getenv("NEO4J_PASSWORD")
ollama_base_url = os.getenv("OLLAMA_BASE_URL")
embedding_model_name = os.getenv("EMBEDDING_MODEL")
llm_name = os.getenv("LLM")
os.environ["NEO4J_URL"] = url

language = os.getenv("SUMMARY_LANGUAGE")
summary_size = os.getenv("SUMMARY_SIZE")
tags_number = os.getenv("TAGS_NUMBER")

logger = get_logger(__name__)

embeddings, dimension = load_embedding_model(
    embedding_model_name, config={"ollama_base_url": ollama_base_url}, logger=logger
)

class StreamHandler(BaseCallbackHandler):
    def __init__(self, container, initial_text=""):
        self.container = container
        self.text = initial_text

    def on_llm_new_token(self, token: str, **kwargs) -> None:
        self.text += token
        self.container.markdown(self.text)


llm = load_llm(llm_name, logger=logger, config={"ollama_base_url": ollama_base_url})

app = FastAPI()
origins = ["*"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def getQa(file: UploadFile):
    pdf_reader = PdfReader(file.file)

    text = ""
    for page in pdf_reader.pages:
        text += page.extract_text()

    text_splitter = RecursiveCharacterTextSplitter(
        chunk_size=1000, chunk_overlap=200, length_function=len
    )

    chunks = text_splitter.split_text(text=text)

    vector_store = Neo4jVector.from_texts(
        chunks,
        url=url,
        username=username,
        password=password,
        embedding=embeddings,
        index_name="pdf_bot",
        node_label="PdfBotChunk",
        pre_delete_collection=True,
    )
    qa = RetrievalQA.from_chain_type(
        llm=llm, chain_type="stuff", retriever=vector_store.as_retriever()
    )
    return qa

@app.post("/classify")
async def classify(file: UploadFile, termList: str):

    qa = getQa(file)
    stream_handler = StreamHandler(st.empty())

    term_query = ("Pick one of the following list of categories: " + termList + ". " +
              "Write the answer only in " + language + " language. " + 
              "Don't add any explanation for the choice in the answer. " + 
              "Don't add any note after the word in the answer. " +
              "Don't add any space before the word in the answer. " +
              "Don't add in the answer the translation of the word in a different language after chosen word. " +
              "Give the answer exactly as a single word from the list.")

    term = qa.run(term_query, callbacks=[stream_handler])
    return {"term": term, "model": llm_name}

@app.post("/prompt")
async def prompt(file: UploadFile, prompt: str):

    qa = getQa(file)
    stream_handler = StreamHandler(st.empty())

    prompt = (prompt + 
              ". Write the answer only in " + language + " language. " + 
              "Don't add any translation to the answer.")

    answer = qa.run(prompt, callbacks=[stream_handler])
    return {"answer": answer, "model": llm_name}

@app.post("/summary")
async def summary(file: UploadFile):

    qa = getQa(file)
    stream_handler = StreamHandler(st.empty())

    summary_query = "Write a short summary of the text in " + summary_size + " words only in " + language
    summary_result = qa.run(summary_query, callbacks=[stream_handler])

    tags_query = ("Provide " + tags_number + " words to categorize the document in language " + language + " in a single line. " +
                 "Use only language " + language + " for these " + tags_number + " words in the answer. " +
                 "Don't add any explanation for the words in the answer. " + 
                 "Don't add any note after the list of words in the answer. " +
                 "Don't use bullets or numbers to list the words in the answer. " +
                 "Don't add in the answer the translation of the words in a different language after the list of words. " +
                 "Give the answer exactly as a list of " + tags_number + " words in language " + language + " separated with comma and without ending dot.")
    tags_result = qa.run(tags_query, callbacks=[stream_handler])

    return {"summary": summary_result, "tags": tags_result, "model": llm_name}
