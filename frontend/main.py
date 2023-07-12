#!/usr/bin/env python3
import flask
import json
import requests

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
    'gAnswer': {'uri': 'http://141.57.8.18:40199/gAnswer/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov': {'uri': 'http://141.57.8.18:40199/deeppavlov2023/answer', 'request_formatter': qsw_request_formatter},
    'deeppavlov2.0': {'uri': 'http://141.57.8.18:40199/deeppavlov2023/answer', 'request_formatter': qsw_request_formatter}
    #'tebaqa': {}
}

app = flask.Flask(__name__)



def neamt_service(form):
    query = form['query']
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

    return {
        'answer': data['answer'],
        'json': data,
    }

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
