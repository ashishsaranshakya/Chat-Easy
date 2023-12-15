import { createAPIError } from '../utils/APIError.js';


export const validateRegisterData = async (req, res, next)=>{
	const {username, password} = req.body;
	if(!username || !password) return next(createAPIError(400, false, 'All fields should be filled'));
	if(password.length<8) return next(createAPIError(400, false, 'Password should be at least 8 characters long.'));
    next();
}

export const validateLoginData = async (req, res, next)=>{
	const {username, password} = req.body;
	if(!username || !password) return next(createAPIError(400, false, 'All fields should be filled'));
	next();
}