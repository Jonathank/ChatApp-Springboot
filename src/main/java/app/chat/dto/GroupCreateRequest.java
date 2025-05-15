package app.chat.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private String groupname;
    private String createdBy;
    private String avatarBase64;
    private String avatarContentType;
}