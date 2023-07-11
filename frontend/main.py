#!/usr/bin/env python3
import flask
import json
import requests

neamt_api = 'http://porque.cs.upb.de/porque-neamt/custom-pipeline'
poolparty_api = 'https://demos-02.poolparty.biz/'

poolparty_projects = requests.get(poolparty_api + 'projects/').json()['projects']

app = flask.Flask(__name__)

def neamt_service(form):
    data = requests.post(neamt_api, data={
        'query': form['query'],
        'components': form['neamt_components'],
        'full_json': 'true',
    }).json()
    return {
        'answer': data['translated_text'],
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
    }
    if 'query' in flask.request.form:
        data = services[flask.request.form['service']](flask.request.form)
    if 'json' in data:
        data['json'] = json.dumps(data['json'], indent=4, sort_keys=True)
    return flask.render_template('qa.html', **data)

if __name__ == '__main__':
    app.run(debug=True, port=8080)
