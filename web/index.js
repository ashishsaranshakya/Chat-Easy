import dotenv from 'dotenv';
dotenv.config();

import app from './src/app.js';
import { mongoConnect } from './src/services/mongoDB.js';

const server = app.listen(process.env.PORT | 3000,
    async() => {
        await mongoConnect();
        console.log(`Server running on PORT ${process.env.PORT | 3000}...`)
    }
);

import io from './src/socket.js';
io(server);