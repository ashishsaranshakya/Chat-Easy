import jwt from 'jsonwebtoken';
import { createAPIError } from '../utils/APIError.js';

export const verifyToken = (req, res, next) => {
    try{
        const token = req.headers['authorization'];
        if (!token) {
            return next(createAPIError(401, false, 'Unauthorized'));
        }

        const verified = jwt.verify(
            token,
            process.env.JWT_SECRET,
            { ignoreExpiration: false }
        );
        req.user = verified;

        next();
    }
    catch(err){
        next(err);
    }
};

export const verifyTokenSocket = (socket, next) => {
    try{
        const token = socket.handshake.auth.session || socket.handshake.headers.authorization;
        if (!token) {
            return next(createAPIError(401, false, 'Unauthorized'));
        }

        const verified = jwt.verify(
            token,
            process.env.JWT_SECRET,
            { ignoreExpiration: false }
        );
        socket.user = verified;

        next();
    }
    catch(err){
        next(err);
    }
}