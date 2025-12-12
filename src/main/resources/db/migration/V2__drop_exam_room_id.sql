-- Drop examRoomId column to fully remove exam room dependency
ALTER TABLE `exam_participants`
    DROP COLUMN `examRoomId`;
