'use strict';

// 편집 중인 값은 브라우저에서 관리하고, 저장·검증·공개는 기존 API로 처리한다.
(() => {
    const editor = document.getElementById('editor');
    const form = document.getElementById('story-form');
    const nodes = document.getElementById('nodes');
    const buttons = ['save', 'validate', 'publish', 'preview'].map(id => document.getElementById(id));
    const emotions = {SAD:'슬픔', HAPPY:'기쁨', ANGRY:'화남', ANXIOUS:'불안', CALM:'안정', LONELY:'외로움', EXCITED:'신남', EMBARRASSED:'부끄러움', FEAR:'두려움', SURPRISED:'놀라움'};
    let revision;
    let dirty = false;
    let busy = true;
    let validated = false;
    let draft;
    let currentSceneId;
    let scenePath = [];
    let validationIssues = [];

    function element(tag, text, className) {
        const item = document.createElement(tag);
        if (text != null) item.textContent = text;
        if (className) item.className = className;
        return item;
    }
    function option(select, value, label) {
        const item = element('option', label);
        item.value = value;
        select.append(item);
    }
    function control(parent, label, name, value, type = 'text', maxLength) {
        const wrapper = element('label', label);
        const input = element(type === 'textarea' ? 'textarea' : type === 'select' ? 'select' : 'input');
        input.name = name;
        if (input.tagName === 'INPUT') input.type = type;
        if (type === 'textarea') input.rows = 4;
        if (maxLength) input.maxLength = maxLength;
        if (name === 'emotion') {
            option(input, '', '감정 선택');
            Object.entries(emotions).forEach(([key, label]) => option(input, key, label));
        }
        input.value = value ?? '';
        wrapper.append(input);
        parent.append(wrapper);
        return input;
    }
    function action(parent, label, callback, danger = false) {
        const button = element('button', label, danger ? 'danger' : 'secondary');
        button.type = 'button';
        button.addEventListener('click', callback);
        parent.append(button);
        return button;
    }
    function value(parent, name) {
        return [...parent.querySelectorAll('[name]')].find(input => input.name === name)?.value ?? '';
    }
    function tell(message, error = false) {
        const target = document.getElementById('message');
        target.textContent = message;
        target.classList.toggle('error', error);
    }
    function refreshState() {
        buttons.forEach(button => { button.disabled = busy || revision == null; });
        document.getElementById('publish').disabled = busy || !validated || dirty;
        form.inert = busy;
        document.getElementById('save-state').textContent = revision == null ? '불러오는 중' : `초안 #${draft.id} · 저장 버전 ${revision}${dirty ? ' · 저장하지 않은 변경 사항' : ' · 저장됨'}`;
    }
    function changed() {
        dirty = true;
        validated = false;
        validationIssues = [];
        document.getElementById('validation').hidden = true;
        document.getElementById('preview-panel').hidden = true;
        refreshState();
        refreshNavigation();
    }
    async function request(url, method = 'GET', body) {
        const headers = {'Accept':'application/json'};
        if (method !== 'GET') {
            const csrfResponse = await fetch(editor.dataset.csrf, {credentials:'same-origin', cache:'no-store'});
            if (!csrfResponse.ok) throw new Error('인증 정보를 가져오지 못했어요. 로그인 상태를 확인하세요.');
            const csrf = await csrfResponse.json();
            headers[csrf.headerName] = csrf.token;
            headers['Content-Type'] = 'application/json';
        }
        const response = await fetch(url, {method, headers, credentials:'same-origin', cache:'no-store', body:body === undefined ? undefined : JSON.stringify(body)});
        if (!response.headers.get('Content-Type')?.includes('application/json')) {
            throw new Error('서버 응답을 확인하지 못했어요. 관리자 API의 연결 주소를 확인하세요.');
        }
        const data = await response.json();
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) document.getElementById('login-notice').hidden = false;
            if (data.errors?.length) showIssues(data.errors);
            if (response.status === 409) throw new Error('다른 창에서 이 초안이 변경되었어요. 현재 내용을 별도로 보관한 후 새로고침하세요. 자동으로 덮어쓰지 않습니다.');
            throw new Error(data.message || '요청에 실패했어요. 다시 시도하세요.');
        }
        document.getElementById('login-notice').hidden = true;
        return data;
    }
    async function run(task) {
        if (busy) return;
        // 다른 장면의 숨겨진 입력에 오류가 있어도 해당 장면을 먼저 열어 안내한다.
        const invalid = [...form.querySelectorAll('input,select,textarea')].find(input => !input.validity.valid);
        if (invalid) {
            const card = invalid.closest('.node-card');
            if (card) openScene(card.dataset.id);
            else document.getElementById('story-settings').open = true;
            invalid.reportValidity();
            return tell('입력 길이와 숫자 범위를 확인하세요.', true);
        }
        busy = true;
        refreshState();
        try { await task(); }
        catch (error) { tell(error.message, true); }
        finally { busy = false; refreshState(); }
    }
    function scene(id) {
        return [...nodes.children].find(card => card.dataset.id === id);
    }
    function sceneTitle(card) {
        if (!card) return '연결되지 않은 장면';
        return value(card, 'title') || `장면 ${[...nodes.children].indexOf(card) + 1}`;
    }
    function sceneGraph() {
        return [...nodes.children].map(card => ({id:card.dataset.id,
            nextNodeIds:value(card, 'type') === 'STORY'
                ? [...card.querySelectorAll('[name="nextNodeId"]')].map(input => input.value) : []}));
    }
    // 실제로 따라온 경로를 우선 사용하고, 구조 보기에서 이동하면 시작점부터 경로를 찾는다.
    function scenePathTo(id) {
        if (currentSceneId === id && scenePath.length) return scenePath;
        return EmotoryNavigation.findPath(sceneGraph(), value(form, 'rootNodeId'), id);
    }
    function openScene(id, path) {
        const card = scene(id);
        if (!card) {
            if (id) tell('연결된 장면이 없어요. 새 장면을 만들거나 기존 장면에 연결하세요.', true);
            return;
        }
        const nextPath = path || scenePathTo(id);
        // 잘못 연결한 순환 경로를 따라가도 이동 경로가 끝없이 늘어나지 않게 한다.
        scenePath = nextPath.filter(nodeId => scene(nodeId)).reduce((result, nodeId) => EmotoryNavigation.follow(result, nodeId), []);
        currentSceneId = id;
        [...nodes.children].forEach(item => { item.hidden = item !== card; });
        refreshNavigation();
        document.getElementById('current-scene-title').scrollIntoView({behavior:'smooth', block:'start'});
    }
    function refreshNavigation() {
        const card = scene(currentSceneId);
        document.getElementById('current-scene-title').textContent = card ? sceneTitle(card) : '이야기 장면';
        document.getElementById('root-notice').hidden = Boolean(scene(value(form, 'rootNodeId')));
        document.getElementById('previous-scene').disabled = scenePath.length < 2;
        document.getElementById('go-root').disabled = !scene(value(form, 'rootNodeId')) || currentSceneId === value(form, 'rootNodeId');
        const breadcrumb = document.getElementById('scene-path');
        breadcrumb.replaceChildren();
        scenePath.forEach((id, index) => {
            if (index) breadcrumb.append(element('span', '›', 'path-separator'));
            const button = action(breadcrumb, sceneTitle(scene(id)), () => openScene(id, scenePath.slice(0, index + 1)));
            if (index === scenePath.length - 1) button.setAttribute('aria-current', 'step');
        });
        nodes.querySelectorAll('.choice-row').forEach(row => {
            const target = scene(value(row, 'nextNodeId'));
            row.querySelector('[data-action="create-scene"]').hidden = Boolean(target);
            const visit = row.querySelector('[data-action="open-scene"]');
            visit.hidden = !target;
            visit.textContent = target ? `${sceneTitle(target)} 열기 →` : '다음 장면 열기 →';
        });
        if (!document.getElementById('structure-panel').hidden) renderStructure();
    }
    function renderStructure() {
        const list = document.getElementById('structure-list');
        list.replaceChildren();
        const root = value(form, 'rootNodeId');
        const reached = EmotoryNavigation.reachable(sceneGraph(), root);
        [...nodes.children].forEach(card => {
            const item = element('article', null, 'structure-item');
            const id = card.dataset.id;
            const status = id === root ? '시작' : value(card, 'type') === 'ENDING' ? '결말' : '장면';
            const hasError = validationIssues.some(issue => issue.nodeId === id);
            action(item, `${status} · ${sceneTitle(card)}${id === currentSceneId ? ' (편집 중)' : ''}`, () => openScene(id));
            if (!reached.has(id)) item.append(element('span', '시작 장면에서 연결되지 않음', 'structure-warning'));
            if (hasError) item.append(element('span', '검증 오류', 'structure-warning'));
            if (value(card, 'type') === 'STORY') {
                const branches = element('ul');
                [...card.querySelectorAll('.choice-row')].forEach(row => {
                    const branch = element('li');
                    const target = scene(value(row, 'nextNodeId'));
                    branch.append(element('span', `${value(row, 'text') || '내용 없는 선택지'} → `));
                    if (target) action(branch, sceneTitle(target), () => openScene(target.dataset.id, [...scenePathTo(id), target.dataset.id]));
                    else branch.append(element('span', '다음 장면 미작성', 'structure-warning'));
                    branches.append(branch);
                });
                item.append(branches);
            }
            list.append(item);
        });
    }
    function refreshTargets() {
        const entries = [...nodes.children].map(card => [card.dataset.id, value(card, 'title') || `장면 ${[...nodes.children].indexOf(card) + 1}`]);
        const selects = [document.getElementById('root-node'), ...nodes.querySelectorAll('[name="nextNodeId"]')];
        selects.forEach(select => {
            const selected = select.value || select.dataset.initial || '';
            select.replaceChildren();
            option(select, '', '장면 선택');
            entries.forEach(([id, title]) => option(select, id, title));
            if (selected && !entries.some(([id]) => id === selected)) option(select, selected, '연결된 장면이 삭제되었습니다');
            select.value = selected;
            delete select.dataset.initial;
        });
        refreshNavigation();
    }
    function addChoice(container, choice = {}) {
        if (container.children.length >= 10) return tell('선택지는 장면당 최대 10개까지 만들 수 있어요.', true);
        const row = element('div', null, 'choice-row');
        row.dataset.id = choice.id || crypto.randomUUID();
        control(row, '선택지 문구', 'text', choice.text, 'text', 255);
        const target = control(row, '다음 장면', 'nextNodeId', '', 'select');
        target.dataset.initial = choice.nextNodeId || '';
        const connection = element('details', null, 'existing-connection');
        connection.append(element('summary', '기존 장면에 연결 / 연결 변경'), target.parentElement);
        row.append(connection);
        const actions = element('div', null, 'actions');
        const create = action(actions, '다음 장면 만들기 →', () => {
            const parent = row.closest('.node-card');
            const child = addNode({title:value(row, 'text') || '새 장면'});
            if (!child) return;
            target.value = child.dataset.id;
            refreshTargets(); changed();
            openScene(child.dataset.id, [...scenePathTo(parent.dataset.id), child.dataset.id]);
        });
        create.dataset.action = 'create-scene';
        const visit = action(actions, '다음 장면 열기 →', () => {
            const parent = row.closest('.node-card');
            openScene(target.value, [...scenePathTo(parent.dataset.id), target.value]);
        });
        visit.dataset.action = 'open-scene';
        row.append(actions);
        action(row, '선택지 삭제', () => { row.remove(); changed(); }, true);
        container.append(row);
        refreshTargets();
    }
    function addAdvice(container, advice = {}) {
        if (container.children.length >= 20) return tell('조언은 최대 20개까지 입력할 수 있어요.', true);
        const row = element('div', null, 'advice-row');
        control(row, '조언 제목', 'adviceTitle', advice.title, 'text', 255);
        control(row, '조언 내용', 'adviceDescription', advice.description, 'textarea', 300);
        action(row, '조언 삭제', () => { row.remove(); changed(); }, true);
        container.append(row);
    }
    function addNode(node = {}) {
        if (nodes.children.length >= 1000) return tell('장면은 최대 1,000개까지 만들 수 있어요.', true);
        const card = element('fieldset', null, 'node-card');
        card.dataset.id = node.id || crypto.randomUUID();
        card.append(element('legend', '이야기 장면'));
        const heading = element('div', null, 'section-heading');
        heading.append(element('span', '이 장면을 작성한 뒤 선택지로 다음 이야기를 이어가세요.', 'muted'));
        action(heading, '장면 삭제', () => {
            if (card.dataset.id === value(form, 'rootNodeId')) return tell('시작 장면은 삭제할 수 없어요. 기본 정보에서 다른 시작 장면을 지정한 후 삭제하세요.', true);
            if (!confirm('이 장면을 삭제할까요? 이 장면으로 연결된 선택지도 확인해 주세요.')) return;
            const previous = scenePath.filter(id => id !== card.dataset.id && scene(id)).at(-1);
            card.remove(); refreshTargets(); changed();
            openScene(previous || value(form, 'rootNodeId') || nodes.firstElementChild?.dataset.id);
        }, true);
        card.append(heading);
        control(card, '장면 이름', 'title', node.title, 'text', 255);
        const type = control(card, '장면 종류', 'type', '', 'select');
        option(type, '', '종류 선택'); option(type, 'STORY', '일반 장면'); option(type, 'ENDING', '결말');
        type.value = node.type ?? 'STORY';
        control(card, '장면 감정 (선택)', 'emotion', node.emotion, 'select');
        const content = control(card, '장면 내용', 'content', node.content, 'textarea', 20000);
        const choiceFields = element('div');
        const choices = element('div', null, 'choices');
        choiceFields.append(element('h3', '선택지'), choices);
        action(choiceFields, '+ 선택지 추가', () => { addChoice(choices); changed(); });
        const ending = element('div', null, 'ending-fields');
        ending.append(element('h3', '결말과 감정 조언'));
        control(ending, '결말 요약', 'summary', node.ending?.summary, 'textarea', 300);
        control(ending, '감정 설명', 'emotionLabel', node.ending?.emotionLabel, 'text', 255);
        control(ending, '결말 본문', 'contentSummary', node.ending?.contentSummary, 'textarea', 20000);
        const advices = element('div', null, 'advices');
        ending.append(element('h4', '감정 조언'), advices);
        action(ending, '+ 조언 추가', () => { addAdvice(advices); changed(); });
        card.append(choiceFields, ending);
        nodes.append(card);
        (node.choices || []).forEach(choice => addChoice(choices, choice));
        (node.ending?.advices || []).forEach(advice => addAdvice(advices, advice));
        const toggleType = () => {
            choiceFields.hidden = type.value !== 'STORY';
            ending.hidden = type.value !== 'ENDING';
            content.parentElement.hidden = type.value === 'ENDING';
        };
        type.addEventListener('change', toggleType);
        toggleType(); refreshTargets();
        card.hidden = card.dataset.id !== currentSceneId;
        return card;
    }
    function documentData() {
        const number = name => value(form, name) === '' ? null : Number(value(form, name));
        return {
            title:value(form, 'title'), description:value(form, 'description'), emotion:value(form, 'emotion') || null,
            maxDepth:number('maxDepth'), maxNodes:number('maxNodes'), rootNodeId:value(form, 'rootNodeId') || null,
            nodes:[...nodes.children].map(card => {
                const type = value(card, 'type') || null;
                return {id:card.dataset.id, title:value(card, 'title'), content:value(card, 'content'), emotion:value(card, 'emotion') || null, type,
                    choices:type === 'STORY' ? [...card.querySelector('.choices').children].map(row => ({id:row.dataset.id, text:value(row, 'text'), nextNodeId:value(row, 'nextNodeId') || null})) : [],
                    ending:type === 'ENDING' ? {summary:value(card, 'summary'), emotionLabel:value(card, 'emotionLabel'), contentSummary:value(card, 'contentSummary'),
                        advices:[...card.querySelector('.advices').children].map(row => ({title:value(row, 'adviceTitle'), description:value(row, 'adviceDescription')}))} : null};
            })
        };
    }
    async function save() {
        if (!dirty) return;
        const result = await request(editor.dataset.api, 'PUT', {revision, document:documentData()});
        revision = result.revision;
        dirty = false;
        validated = false;
    }
    function showIssues(issues) {
        validationIssues = issues;
        refreshNavigation();
        document.getElementById('validation').hidden = false;
        const list = document.getElementById('issues');
        list.replaceChildren();
        document.getElementById('validation-summary').textContent = issues.length ? `${issues.length}개 항목을 확인하세요. 오류를 누르면 해당 장면으로 이동합니다.` : '검증을 통과했어요. 현재 저장 버전을 공개할 수 있습니다.';
        issues.forEach(issue => {
            const item = element('li');
            const nodeIndex = issue.field?.match(/^nodes\[(\d+)\]/);
            const card = [...nodes.children].find(card => card.dataset.id === issue.nodeId)
                || (nodeIndex ? nodes.children[Number(nodeIndex[1])] : null);
            const title = card ? value(card, 'title') || '이름 없는 장면' : '이야기 정보';
            action(item, `${title}${issue.choiceId ? ' / 선택지' : ''} · ${issue.message}`, () => {
                if (card) openScene(card.dataset.id);
                else document.getElementById('story-settings').open = true;
                const choice = card && [...card.querySelectorAll('.choice-row')].find(row => row.dataset.id === issue.choiceId);
                const adviceIndex = issue.field?.match(/advices\[(\d+)\]\.(title|description)$/);
                const advice = card && adviceIndex ? card.querySelector('.advices').children[Number(adviceIndex[1])] : null;
                const target = choice || advice || card || form;
                document.querySelectorAll('.validation-focus').forEach(item => item.classList.remove('validation-focus'));
                target.classList.add('validation-focus');
                target.scrollIntoView({behavior:'smooth', block:'center'});
                const field = advice ? (adviceIndex[2] === 'title' ? 'adviceTitle' : 'adviceDescription')
                    : (issue.field || '').replace(/^nodes\[\d+\]\./, '').replace(/^ending\./, '');
                const input = [...target.querySelectorAll('[name]')].find(input => input.name === field);
                (input || target.querySelector('input,select,textarea'))?.focus({preventScroll:true});
            });
            list.append(item);
        });
    }
    function showPreview(nodeId) {
        const doc = documentData();
        const node = doc.nodes.find(node => node.id === nodeId);
        if (!node) return tell('시작 장면과 선택지 연결을 확인하세요.', true);
        document.getElementById('preview-panel').hidden = false;
        document.getElementById('preview-title').textContent = node.title || '이야기 장면';
        document.getElementById('preview-content').textContent = node.type === 'ENDING'
            ? [node.ending.contentSummary, node.ending.summary, node.ending.emotionLabel, ...node.ending.advices.map(a => `${a.title}\n${a.description}`)].join('\n\n') : node.content;
        const choices = document.getElementById('preview-choices');
        choices.replaceChildren();
        node.choices.forEach(choice => action(choices, choice.text || '내용 없는 선택지', () => showPreview(choice.nextNodeId)));
        document.getElementById('preview-panel').scrollIntoView({behavior:'smooth'});
    }
    form.addEventListener('submit', event => event.preventDefault());
    form.addEventListener('input', changed);
    form.addEventListener('change', event => {
        refreshTargets(); changed();
        if (event.target.id === 'root-node') openScene(event.target.value);
    });
    function createRoot() {
        const card = addNode({title:'시작 장면'});
        if (!card) return;
        document.getElementById('root-node').value = card.dataset.id;
        changed(); openScene(card.dataset.id, [card.dataset.id]);
    }
    document.getElementById('create-root').addEventListener('click', createRoot);
    document.getElementById('go-root').addEventListener('click', () => openScene(value(form, 'rootNodeId'), [value(form, 'rootNodeId')]));
    document.getElementById('previous-scene').addEventListener('click', () => {
        if (scenePath.length > 1) openScene(scenePath.at(-2), scenePath.slice(0, -1));
    });
    document.getElementById('toggle-structure').addEventListener('click', event => {
        const panel = document.getElementById('structure-panel');
        panel.hidden = !panel.hidden;
        event.currentTarget.setAttribute('aria-expanded', String(!panel.hidden));
        event.currentTarget.textContent = panel.hidden ? '전체 구조 보기' : '전체 구조 닫기';
        if (!panel.hidden) renderStructure();
    });
    document.getElementById('save').addEventListener('click', () => run(async () => { await save(); tell('초안을 저장했어요.'); }));
    document.getElementById('validate').addEventListener('click', () => run(async () => {
        await save();
        const result = await request(`${editor.dataset.api}/validate`, 'POST', {revision});
        validated = result.valid;
        showIssues(result.errors);
        tell(result.valid ? '검증을 통과했어요. 공개 버튼을 누르면 사용자 목록에 반영됩니다.' : '검증 오류를 수정한 후 다시 검증하세요.', !result.valid);
    }));
    document.getElementById('publish').addEventListener('click', () => {
        if (!validated || dirty || !confirm('검증한 이야기를 공개할까요? 사용자가 이 이야기를 플레이할 수 있게 됩니다.')) return;
        run(async () => {
            const result = await request(`${editor.dataset.api}/publish`, 'POST', {revision});
            tell(`공개 완료! 동화 #${result.storyId} · 공개 버전 ${result.version}. 사용자 이야기 목록에서 확인할 수 있어요.`);
        });
    });
    document.getElementById('preview').addEventListener('click', () => showPreview(value(form, 'rootNodeId')));
    document.getElementById('restart-preview').addEventListener('click', () => showPreview(value(form, 'rootNodeId')));
    window.addEventListener('beforeunload', event => { if (dirty) { event.preventDefault(); event.returnValue = ''; } });
    (async () => {
        try {
            draft = await request(editor.dataset.api);
            revision = draft.revision;
            const doc = draft.document;
            const emotionSelect = form.elements.namedItem('emotion');
            option(emotionSelect, '', '감정 선택');
            Object.entries(emotions).forEach(([key, label]) => option(emotionSelect, key, label));
            for (const name of ['title','description','emotion','maxDepth','maxNodes']) form.elements.namedItem(name).value = doc[name] ?? '';
            document.getElementById('root-node').dataset.initial = doc.rootNodeId || '';
            doc.nodes.forEach(addNode);
            refreshTargets(); form.hidden = false;
            if (!doc.nodes.length) {
                createRoot();
                document.getElementById('story-settings').open = true;
                tell('시작 장면을 준비했어요. 이야기를 작성하고 선택지에서 다음 장면을 만들어 보세요. 초안 저장을 누르면 서버에 저장됩니다.');
            } else {
                openScene(scene(doc.rootNodeId) ? doc.rootNodeId : nodes.firstElementChild.dataset.id);
                tell('현재 장면을 작성하고 선택지로 이야기를 이어가세요. 다른 갈래는 이전 장면이나 전체 구조 보기에서 이동할 수 있어요.');
            }
        } catch (error) { tell(error.message, true); }
        finally { busy = false; refreshState(); }
    })();
})();
