const webSocketServer = require('websocket').server;
const http = require('http');
const server = http.createServer().listen(8000);
const wsServer = new webSocketServer({
  httpServer: server
});

// Generates unique ID for every new connection
const getUniqueID = () => {
  const s4 = () => Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
  return s4() + s4() + '-' + s4();
};

const clients = {};

const sendMessage = (json, userId) => {
  // Sending the current data to all other connected clients
  Object.keys(clients).map((client) => {
    if (client !== userId)
      clients[client].sendUTF(json);
  });
};

wsServer.on('request', function(request) {
  const userId = getUniqueID();
  console.log(`${new Date()} Recieved a new connection from origin ${request.origin}.`);
  // Accept the requests from all origin
  const connection = request.accept(null, request.origin);
  clients[userId] = connection;
  console.log(`Connected: ${userId} in ${Object.getOwnPropertyNames(clients)}`);
  connection.on('message', function(message) {
    if (message.type === 'utf8') {
      try {
        console.log('receive', message.utf8Data);
        const dataFromClient = JSON.parse(message.utf8Data);
        sendMessage(JSON.stringify(dataFromClient), userId);
      } catch (e) {
        console.error(e.message)
      }
    }
  });
  // user disconnected
  connection.on('close', function(connection) {
    console.log(`${new Date()} Peer ${userId} disconnected.`);
  });
});
