CREATE OR REPLACE FUNCTION notify_on_group_invitation()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.inviter_id != NEW.invited_user_id THEN
        INSERT INTO notifications (user_id, sender_id, type, content)
        VALUES (NEW.invited_user_id, NEW.inviter_id, 'GROUP_INVITATION', 'te invitó a un grupo de estudio');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS tr_notify_on_group_invitation ON group_invitations;
CREATE TRIGGER tr_notify_on_group_invitation
AFTER INSERT ON group_invitations
FOR EACH ROW EXECUTE FUNCTION notify_on_group_invitation();
