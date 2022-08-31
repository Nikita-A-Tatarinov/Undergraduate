import argparse
import json
import nltk
import os
import random
import re
import string
from sklearn.feature_extraction.text import TfidfVectorizer


def initialize_parser():
    parser = argparse.ArgumentParser(description='Retrieves keywords from text data and generates MCQ-s')
    parser.add_argument('--datapath', default='lecture.txt', help='Path to text data')
    parser.add_argument('--outputdir', default='./', help='Relative directory for output files')
    parser.add_argument('--nkeywords', type=int, default=10, help='Preferable (and maximum) number of keywords')
    parser.add_argument('--minngrams', type=int, default=1, help='Minimum number of words in a keyword')
    parser.add_argument('--maxngrams', type=int, default=3, help='Maximum number of words in a keyword')
    parser.add_argument('--nitems', type=int, default=10, help='Preferable (and maximum) number of MCQ-s to generate')
    parser.add_argument('--noptions', type=int, default=4, help='Number of options in a MCQ')
    args = parser.parse_args()
    return parser, args


def initialize_tokenizer():
    nltk.download('stopwords')
    stop_words = nltk.corpus.stopwords.words('english')
    tokenizer = nltk.WordPunctTokenizer()
    return stop_words, tokenizer


def initialize_lemmatizer():
    nltk.download('omw-1.4')
    nltk.download('wordnet')
    lemmatizer = nltk.stem.WordNetLemmatizer()
    return lemmatizer


def initialize_vectorizer(max_num_of_keywords, min_n, max_n):
    vectorizer = TfidfVectorizer(max_features=max_num_of_keywords, ngram_range=(min_n, max_n))
    return vectorizer


def read_data(file_name):
    with open(file_name) as f:
        data = f.read()
    return data


def split_data_into_documents(data):
    documents = data.replace('!', '.').replace('?', '.').replace('…', '.').replace('\n', ' ') \
        .replace('\'', ' ').replace('"', ' ').replace('—', ' ').split('.')
    for i in range(0, len(documents)):
        tokens = documents[i].split()
        documents[i] = ' '.join(tokens)
    documents = list(filter(None, documents))
    return documents


def split_documents_into_tokens(documents):
    tokens = []
    for document in documents:
        cur_tokens = tokenizer.tokenize(document.lower())
        cur_tokens = [token for token in cur_tokens if
                      (token not in string.punctuation and token not in stop_words)]
        tokens.append(cur_tokens)
    return tokens


def normalize_tokens(tokens, lemmatizer):
    norm_tokens = []
    for i in range(0, len(tokens)):
        norm_tokens.append([])
        for token in tokens[i]:
            norm_tokens[i].append(lemmatizer.lemmatize(token))
    return norm_tokens


def normalize_documents(norm_tokens):
    norm_documents = []
    for norm_toks in norm_tokens:
        norm_documents.append(' '.join(norm_toks))
    return norm_documents


def retrieve_keywords(norm_documents, vectorizer):
    vectorizer.fit(norm_documents)
    keywords = list(vectorizer.get_feature_names_out())
    return keywords


def split_data_into_sentences(data):
    sentences = re.split('…|\.\.\.|\?|!|\.', data.replace('\n', ''))
    return sentences


def generate_items(sentences, keywords, max_num_of_items, num_of_options):
    i = 0
    stems = []
    options = []
    for sentence in sentences:
        words = re.split('\W', sentence)  # Not alhpanumerical characters and not '_'
        while '' in words:
            words.remove('')
        for word in words:
            normalized_word = lemmatizer.lemmatize(word.lower())
            if normalized_word in keywords:
                index = sentence.find(word)
                stems.append(sentence[:index] + (7 * '_') + sentence[index + len(word):])
                options.append([word])
                while len(options[i]) != num_of_options:
                    distractor = keywords[random.randint(0, len(keywords) - 1)]
                    if distractor != normalized_word and distractor not in options[i]:
                        options[i].append(distractor)
                random.shuffle(options[i])
                i += 1
                if i == max_num_of_items:
                    break
        if i == max_num_of_items:
            break
    return stems, options


if __name__ == '__main__':
    parser, args = initialize_parser()
    os.makedirs(args.outputdir, exist_ok=True)

    # 1. Reading data from file
    data = read_data(args.datapath)

    # 2. Tokenization, data cleaning and removing stop words
    stop_words, tokenizer = initialize_tokenizer()
    documents = split_data_into_documents(data)
    print('Documents:')
    print(documents[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'documents.json'), 'w') as f:
        json.dump(documents, f)
    tokens = split_documents_into_tokens(documents)
    print('Tokens:')
    print(tokens[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'tokens.json'), 'w') as f:
        json.dump(tokens, f)

    # 3. Word normalization
    lemmatizer = initialize_lemmatizer()
    norm_tokens = normalize_tokens(tokens, lemmatizer)
    print('Normalized tokens:')
    print(norm_tokens[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'norm_tokens.json'), 'w') as f:
        json.dump(norm_tokens, f)
    norm_documents = normalize_documents(norm_tokens)
    print('Normalized documents:')
    print(norm_documents[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'norm_documents.json'), 'w') as f:
        json.dump(norm_documents, f)

    # 4. Retrieving keywords
    vectorizer = initialize_vectorizer(args.nkeywords, args.minngrams, args.maxngrams)
    keywords = retrieve_keywords(norm_documents, vectorizer)
    print('Keywords:')
    print(keywords[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'keywords.json'), 'w') as f:
        json.dump(keywords, f)

    # 5. Generating items
    sentences = split_data_into_sentences(data)
    print('Sentences:')
    print(sentences[:10], end='\n\n')
    with open(os.path.join(args.outputdir, 'sentences.json'), 'w') as f:
        json.dump(sentences, f)
    stems, options = generate_items(sentences, keywords, args.nitems, args.noptions)
    with open(os.path.join(args.outputdir, 'items.txt'), 'w') as f:
        for i in range(0, len(stems)):
            f.write(stems[i] + '\n')
            for j in range(0, args.noptions):
                f.write('\t' + options[i][j] + '\n')
            f.write('\n')
