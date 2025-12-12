-- Make examRoomId column nullable to support standalone exams without room
ALTER TABLE `exam_participants`
    MODIFY COLUMN `examRoomId` BIGINT NULL;
