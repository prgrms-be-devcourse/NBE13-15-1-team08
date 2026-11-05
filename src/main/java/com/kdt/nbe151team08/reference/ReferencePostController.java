package com.kdt.nbe151team08.reference;

import com.kdt.nbe151team08.common.exception.ValidationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestClient 외부 게시글 조회 API.
 *
 * <p>기능:
 * <ul>
 *   <li>{@code GET /api/v1/reference/posts/{postId}} — 외부 서버 게시글 단건 조회</li>
 *   <li>postId는 1 이상 정수. 숫자가 아니면 400 VALIDATION_ERROR, 1 미만이면 fieldErrors.postId</li>
 *   <li>실제 조회·실패 처리는 {@link ReferencePostClient}에 위임</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/reference/posts")
public class ReferencePostController {

    private final ReferencePostClient referencePostClient;

    public ReferencePostController(ReferencePostClient referencePostClient) {
        this.referencePostClient = referencePostClient;
    }

    /**
     * 외부 게시글 조회.
     * 성공 시 외부 응답의 id·userId·title·body를 그대로 반환합니다.
     */
    @GetMapping("/{postId}")
    public ReferencePostResponse getPost(@PathVariable("postId") int postId) {
        if (postId < 1) {
            throw new ValidationException("postId", "postId는 1 이상의 정수여야 합니다.");
        }
        return this.referencePostClient.getPost(postId);
    }
}
