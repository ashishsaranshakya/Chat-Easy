import config from './configs/config.js';
import express from 'express';

import errorHandler from './middlewares/errorHandler.js';
import routeNotFoundHandler from './middlewares/routeNotFoundHandler.js';
import authRoutes from './routes/auth.js';
import chatRoutes from './routes/chat.js';

const app = express();
app.use(express.json());

/* ROUTES */
const apiBaseUrl = `/api/${config.api_version}`;

app.get(`${apiBaseUrl}`,(req, res) => res.status(200).json({success: true, message: 'Chat-Easy API'}))
app.use(`${apiBaseUrl}/auth`, authRoutes);
app.use(`${apiBaseUrl}/chat`, chatRoutes);

app.use(routeNotFoundHandler);
app.use(errorHandler);

export default app;