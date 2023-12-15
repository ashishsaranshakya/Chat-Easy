import express from 'express';
import { login, register } from '../controllers/auth.js';
import { validateRegisterData, validateLoginData } from '../middlewares/authValidator.js';

const router = express.Router();

router.post('/register', validateRegisterData, register);
router.post('/login', validateLoginData, login);

export default router;