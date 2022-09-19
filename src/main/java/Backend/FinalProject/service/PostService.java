package Backend.FinalProject.service;

import Backend.FinalProject.repository.PostRepository;
import Backend.FinalProject.request.PostRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public ResponseDto<?> createpost(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> getpost(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> getpostid(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> updatepost(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> deletepost(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> wishpost(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> application(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> getapplication(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> approve(PostRequestDto requestDto){

        return null;
    }

    @Transactional
    public ResponseDto<?> deny(PostRequestDto requestDto){

        return null;
    }

}
