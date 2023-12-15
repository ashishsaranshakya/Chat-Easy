import dotenv from 'dotenv';
dotenv.config();

export default {
    api_version: process.env.API_VERSION,
    mongoDB: {
        url: process.env.MONGO_URL
    },
    session: {
        secret: process.env.JWT_SECRET
    }
}