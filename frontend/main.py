#!/usr/bin/env python3
import flask
import functools
import json
import requests
import time
import traceback


from SPARQLWrapper import JSON
from SPARQLWrapper import SPARQLWrapper
from SPARQLWrapper.SPARQLExceptions import SPARQLWrapperException

neamt_api = 'http://porque.cs.upb.de/porque-neamt/custom-pipeline'
poolparty_api = 'https://demos-02.poolparty.biz/'

porque_langs = {
    'de': 'German',
    'fr': 'French',
    'es': 'Spanish',
    'en': 'English'
}

sparql_endpoints = {
    'dbpedia_2016-10_enriched': {'label': 'DBpedia 2016-10 (Enriched)', 'uri' : 'http://porque.cs.upb.de:8890/sparql/', 'named_graph' : 'http://www.upb.de/en-dbp2016-10-enriched'},
    'dbpedia_2016-10_normal': {'label': 'DBpedia 2016-10', 'uri' : 'http://porque.cs.upb.de:8890/sparql/', 'named_graph' : 'http://www.upb.de/en-dbp2016-10-normal'},
    'wikidata_qald10': {'label': 'Wikidata (QALD10 instance)',' uri' : 'https://skynet.coypu.org/wikidata/', 'named_graph' : None},
    'wikidata': {'label': 'Wikidata (Latest)', 'uri' : 'https://query.wikidata.org/sparql', 'named_graph' : None},
    'dbpedia': {'label': 'DBpedia (Latest)', 'uri' : 'https://dbpedia.org/sparql', 'named_graph' : None}
}

app = flask.Flask(__name__)

def ttl_hash():
    return round(time.time() / 60)

@functools.lru_cache()
def fetch_poolparty_projects(ttl_hash=None):
    try:
        app.logger.info('Fetching a list of poolparty projects...')
        return requests.get(poolparty_api + 'projects/').json()['projects']
    except Exception:
        app.logger.exception('Failed to fetch a list of poolparty projects')
        return []

def get_poolparty_projects():
    return fetch_poolparty_projects(ttl_hash=ttl_hash())


def send_sparql(sparql_str, endpoint_info):
    try:
        sparql = SPARQLWrapper(endpoint_info['uri'])
        named_graph = sparql_endpoints.get('named_graph')
        if named_graph:
            sparql.setNamedGraph(named_graph)
        sparql.setQuery(sparql_str)
        sparql.setReturnFormat(JSON)
        sparql_results = sparql.query().convert()
        return sparql_results
    except Exception as e:
        print('Exception occurred for \tSPARQL: %s' % (sparql_str))
        print(str(e))
        return {"head": {"vars": []}, "results": {"bindings": []}}

def qsw_request_formatter(question, uri):
    data = requests.get(uri, params={'question': question}).json()
    return {
        'answer': data['answer'],
        'json': data,
    }


def prettify_answers(answers_raw):
    """
    Adapted from: https://github.com/WSE-research/qa-systems-wrapper/blob/84f90d703ebf620162ab085015e2f436dda7ef1e/routers/qanswer.py#L16-L23
    """
    if 'results' in answers_raw.keys() and len(answers_raw['results']['bindings']) > 0:
        # converting to set
        res = set()
        for uri in answers_raw['results']['bindings']:
            res.add(uri[list(uri.keys())[0]]['value'])
        return list(res)
    else:
        return []

def mst5_request_formatter(question, uri):
    payload = {
        'query': question,
        'lang': 'en'
    }
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
    }
    sparql_str = requests.post(uri, headers=headers, data=payload).text
    if 'wikidata' in uri:
        # send sparql to wikidata
        endpoint_info = sparql_endpoints['wikidata']
    else:
        # send sparql to dbpedia
        endpoint_info = sparql_endpoints['dbpedia']

    answer = send_sparql(sparql_str, endpoint_info)
    
    return {
        'answer': prettify_answers(answer),
        'sparql': sparql_str.replace("\n"," ")
    }


lfqa_systems = {
    'mst5-wikidata': {'uri': 'http://porque.cs.upb.de/mst5/wikidata/fetch-sparql', 'request_formatter': mst5_request_formatter},
    'mst5-dbpedia': {'uri': 'http://porque.cs.upb.de/mst5/dbpedia/fetch-sparql', 'request_formatter': mst5_request_formatter},
    'tebaqa': {'uri': 'http://141.57.8.18:40199/tebaqa/answer', 'request_formatter': qsw_request_formatter},
    'gAnswer': {'uri': 'http://141.57.8.18:40199/gAnswer/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov': {'uri': 'http://141.57.8.18:40199/deeppavlov/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov2.0': {'uri': 'http://141.57.8.18:40199/deeppavlov2023/answer', 'request_formatter': qsw_request_formatter},
}

def translate_message(lang, message):
    retval = ''
    if not lang or (lang == 'en'):
        retval = message
    else:
        data = requests.post(neamt_api, data={
            'query': message,
            'components': 'libre_mt',
            'full_json': 'true',
            'target_lang': lang
        }).json()
        print(data)
        query = data['translated_text']
        retval = query
    return retval

def mst5_service(form):
    query = form['query']
    lang = form['mst5_lang_id']
    endpoint_id = form['mst5_sparql_endpoint_id']
    endpoint_info = sparql_endpoints[endpoint_id]
    try:
        payload = {
        'query': query,
        'lang': lang
        }
        headers = {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
        mst5_uri = lfqa_systems['mst5-wikidata']['uri']
        if 'dbpedia' in endpoint_id:
            mst5_uri = lfqa_systems['mst5-dbpedia']['uri']
        
        sparql_str = requests.post(mst5_uri, headers=headers, data=payload).text

        answer = send_sparql(sparql_str, endpoint_info)
        
        data = {
            'answer': prettify_answers(answer),
            'sparql': sparql_str.replace("\n"," ")
        }
        retval = {
            'answer': data['answer'] if len(data['answer']) > 0 else translate_message(data.get('lang'),'No answer found.'),
            'json': data
        }
    except Exception as err:
        # printing stack trace
        traceback.print_exc()
        retval = {
            'answer': translate_message(data.get('lang'), 'Something went wrong, please contact the system administrator.'),
            'json': {'exception': '%s: %s'%(type(err).__name__, str(err))},
            'exception': True
        }
    return retval
    

def neamt_service(form, arg2='neamt_'):
    query = form['query']
    form_prefix = arg2
    lang = form[form_prefix + 'lang_id']
    data = {}
    try:
        data = requests.post(neamt_api, data={
            'query': query,
            'components': form[form_prefix + 'neamt_components'],
            'full_json': 'true',
            'lang': lang
        }).json()
        retval = {
            'answer': data['translated_text'],
            'json': data
        }
    except Exception as err:
        # printing stack trace
        traceback.print_exc()
        retval = {
            'answer': translate_message(data.get('lang'), 'Something went wrong, please contact the system administrator.'),
            'json': {'exception': '%s: %s'%(type(err).__name__, str(err))},
            'exception': err
        }
    return retval

def lfqa_service(form):
    data = {}
    try:
        retval = neamt_service(form, 'lfqa_')

        if retval.get('exception'):
            raise err
        
        data = retval['json']
        query = retval['answer']
        # call the qa system
        qa_sys = lfqa_systems[form['lfqa_qa_system_id']]

        qa_data = qa_sys['request_formatter'](query, qa_sys['uri'])

        data.update(qa_data)
        print(data)
        retval = {
            'answer': data['answer'] if len(data['answer']) > 0 else translate_message(data.get('lang'),'No answer found.'),
            'json': data
        }
    except Exception as err:
        # printing stack trace
        traceback.print_exc()
        retval = {
            'answer': translate_message(data.get('lang'), 'Something went wrong, please contact the system administrator.'),
            'json': {'exception': '%s: %s'%(type(err).__name__, str(err))},
            'exception': True
        }
    return retval

def poolparty_service(form):
    assert 'poolparty_project_id' in form and form['poolparty_project_id'] in get_poolparty_projects()
    data = requests.get(poolparty_api + 'projects/' + form['poolparty_project_id'] + '/ask', params={
        'question': form['query'],
        'lang': form['poolparty_lang'],
        'numdocs': form['poolparty_numdocs'],
    }).json()
    return {
        'answer': data['answer'],
        'json': data,
    }

services = {
    'lfqa': lfqa_service,
    'poolparty': poolparty_service,
    'neamt': neamt_service,
    'mst5': mst5_service,
}

@app.route('/', methods=['GET', 'POST'])
def qa():
    data = {
        'poolparty_projects': get_poolparty_projects(),
        'lfqa_systems': lfqa_systems,
        'porque_langs': porque_langs,
        'sparql_endpoints': sparql_endpoints
    }
    if 'query' in flask.request.form:
        data.update(services[flask.request.form['service']](flask.request.form))
    if 'json' in data:
        data['json'] = json.dumps(data['json'], indent=4, sort_keys=True)
    return flask.render_template('qa.html', **data)

if __name__ == '__main__':
    app.run(debug=True, port=8080)
