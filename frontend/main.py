#!/usr/bin/env python3
import flask
import json
import requests
import traceback

neamt_api = 'http://porque.cs.upb.de/porque-neamt/custom-pipeline'
poolparty_api = 'https://demos-02.poolparty.biz/'

poolparty_projects = requests.get(poolparty_api + 'projects/').json()['projects']

def qsw_request_formatter(question, uri):
    data = requests.get(uri, params={'question': question}).json()
    return {
        'answer': data['answer'],
        'json': data,
    }

lfqa_systems = {
    'tebaqa': {'uri': 'http://141.57.8.18:40199/tebaqa/answer', 'request_formatter': qsw_request_formatter},
    'gAnswer': {'uri': 'http://141.57.8.18:40199/gAnswer/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov': {'uri': 'http://141.57.8.18:40199/deeppavlov2023/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov2.0': {'uri': 'http://141.57.8.18:40199/deeppavlov2023/answer', 'request_formatter': qsw_request_formatter}
}

app = flask.Flask(__name__)

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

def neamt_service(form):
    query = form['query']
    data = {}
    try:
        data = requests.post(neamt_api, data={
            'query': query,
            'components': form['neamt_components'],
            'full_json': 'true',
        }).json()

        query = data['translated_text']
        # call the qa system
        qa_sys = lfqa_systems[form['qa_system_id']]

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
            'json': {'exception': '%s: %s'%(type(err).__name__, str(err))}
        }
    return retval

def poolparty_service(form):
    assert form['poolparty_project_id'] in poolparty_projects
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
    'neamt': neamt_service,
    'poolparty': poolparty_service,
}

@app.route('/', methods=['GET', 'POST'])
def qa():
    data = {
        'poolparty_projects': poolparty_projects,
        'lfqa_systems': lfqa_systems,
    }
    if 'query' in flask.request.form:
        data.update(services[flask.request.form['service']](flask.request.form))
    if 'json' in data:
        data['json'] = json.dumps(data['json'], indent=4, sort_keys=True)
    return flask.render_template('qa.html', **data)

if __name__ == '__main__':
    app.run(debug=True, port=8080)
