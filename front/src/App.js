import React, { useEffect, useState } from 'react';
import './App.css';
import { w3cwebsocket as W3CWebSocket } from "websocket";

const client = new W3CWebSocket('ws://127.0.0.1:8000');

function App() {
    const [isLoading, setIsLoading] = useState(true);
    const [tickFrenquence, setTickFrenquence] = useState(0);
    const [nbThread, setNbThread] = useState(0);
    const [nbPipeline, setNbPipeline] = useState(0);
    const [pipeline, setPipeline] = useState([]);
    const [nbRobot, setNbRobot] = useState(0);
    const [stock, setStock] = useState(0);
    const [aircraft, setAircraft] = useState([]);
    const [robot, setRobot] = useState([]);

    useEffect(() => {

        client.onopen = () => {
            console.log('WebSocket Client Connected');
            setIsLoading(false);
        };

        client.onmessage = (message) => {
            try {
                const data = JSON.parse(message.data);
                console.log('message', data);
                if (data[0] === 0) {
                    setTickFrenquence(data[1]);
                    setNbThread(data[2]);
                    setNbPipeline(data[3]);
                    setNbRobot(data[4]);
                    const newPipelines = [];
                    for (let i = 0; i < data[3]; i++)
                        newPipelines.push({ id: i + 1, status: 'OPEN' });
                    setPipeline(newPipelines);
                    const newRobots = [];
                    for (let i = 0; i < data[4]; i++)
                        newRobots.push({ name: `Robot ${i + 1}`, status: 'IS WAITING', pipelineId: 'none', todo: 0 });
                    setRobot(newRobots);
                }
                if (data[0] === 1)
                    setStock(data[2]);
                if (data[0] === 2)
                    setAircraft([...aircraft, { name: data[1], done: 0, size: data[2], pipelineId: 0 }]);
                if (data[0] === 3) {
                    const newAircrafts = aircraft.map((a) => {
                        if (a.name === data[1])
                            a.pipelineId = data[2];
                        return a;
                    });
                    setAircraft(newAircrafts);
                }
                if (data[0] === 7 && (data[2] === 2 || data[2] === 3)) {
                    const newPipelines = pipeline.map((p) => {
                        if (p.id === data[1]) {
                            if (data[2] === 2)
                                p.status = 'ENDED';
                            if (data[2] === 3)
                                p.status = 'CLOSED';
                            if (data[2] === 1)
                                p.status = 'OPEN';
                        }
                        return p;
                    });
                    setPipeline(newPipelines);
                }
                if (data[0] === 4) {
                    const newRobots = robot.map((r) => {
                        if (r.name === data[2])
                            r.pipelineId = data[1];
                        return r;
                    });
                    setRobot(newRobots);
                }
                if (data[0] === 5) {
                    const newRobots = robot.map((r) => {
                        if (r.name === data[1])
                            r.todo = data[2];
                        return r;
                    });
                    setRobot(newRobots);
                }
                if (data[0] === 6) {
                    const newRobots = robot.map((r) => {
                        if (r.name === data[1]) {
                            if (data[2] === 1)
                                r.status = 'IS ON THE STORAGE';
                            if (data[2] === 2)
                                setStock(stock - 1);
                            if (data[2] === 3) {
                                r.status = `IS WORKING ON ${r.pipelineId}`;
                                r.todo -= 1;
                            }
                            if (data[2] === 4) {
                                r.status = 'HAS ENDED WORK';
                                r.todo = 0;
                            }

                        }
                        return r;
                    });
                    setRobot(newRobots);
                }
                if (data[0] === 7) {
                    if (data[2] === 1) {
                        const newAircrafts = aircraft.map((a) =>
                                a.pipelineId === data[1] ? {...a, done: a.done+1} : a
                        );
                        setAircraft(newAircrafts);
                    }
                    if (data[2] === 2) {
                        const newAircraft = aircraft.map((a) =>
                                a.pipelineId === data[1] ? {...a, done: a.size} : a
                        );
                        setAircraft(newAircraft);
                    }
                }
            } catch (e) {
                console.error(e.message);
            }
        };

        client.onerror = function() {
            console.log('Connection Error');
        };

        client.onclose = function() {
            console.log('Client Closed');
        };

    });

    return (
            <div className="App">
                {isLoading && <div>Loading...</div>}
                {!isLoading &&
                    <div>
                        Tick frequency: {tickFrenquence}<br/>
                        Number of thread: {nbThread}<br/>
                        Number of pipeline: {nbPipeline}<br/>
                        Number of robot: {nbRobot}<br/>
                        Stock: {stock} <br/>
                        <br/>
                        {pipeline.map((p, j) =>
                                <div className="pipeline" key={'pipeline'+j}>
                                    The pipeline {p.id} is {p.status}
                                </div>
                        )}
                        <br/>
                        {robot.map((r, i) =>
                                <div className="robot" key={'robot-'+i}>
                                     {r.name} with {r.todo || '0'} work left to do {r.status}
                                </div>
                        )}
                        <br/>
                        {aircraft.map((a, k) =>
                                <div className="aircraft" key={'aircraft'+k}>
                                    The {a.name} of {a.done}/{a.size} parts is on the {a.pipelineId}
                                </div>
                        )}
                    </div>
                }
            </div>
    );
}

export default App;
