'use strict';

// 순환이나 합쳐지는 분기가 있어도 안전하게 이동 경로와 연결된 장면을 찾는다.
globalThis.EmotoryNavigation = {
    findPath(graph, rootId, targetId) {
        const nodes = new Map(graph.map(node => [node.id, node.nextNodeIds]));
        if (!nodes.has(targetId)) return [];
        const queue = nodes.has(rootId) ? [[rootId]] : [];
        const visited = new Set();
        for (let index = 0; index < queue.length; index++) {
            const path = queue[index];
            const last = path.at(-1);
            if (last === targetId) return path;
            if (visited.has(last)) continue;
            visited.add(last);
            for (const next of nodes.get(last) || []) {
                if (nodes.has(next) && !visited.has(next)) queue.push([...path, next]);
            }
        }
        return [targetId];
    },
    follow(path, targetId) {
        const previous = path.indexOf(targetId);
        return previous < 0 ? [...path, targetId] : path.slice(0, previous + 1);
    },
    reachable(graph, rootId) {
        const nodes = new Map(graph.map(node => [node.id, node.nextNodeIds]));
        const visited = new Set();
        const queue = nodes.has(rootId) ? [rootId] : [];
        for (let index = 0; index < queue.length; index++) {
            const id = queue[index];
            if (visited.has(id)) continue;
            visited.add(id);
            for (const next of nodes.get(id) || []) {
                if (nodes.has(next) && !visited.has(next)) queue.push(next);
            }
        }
        return visited;
    }
};
