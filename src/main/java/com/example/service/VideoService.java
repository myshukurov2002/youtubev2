package com.example.service;import com.example.config.CustomUserDetails;import com.example.dto.ApiResponse;import com.example.dto.VideoDTO;import com.example.entity.ProfileEntity;import com.example.entity.VideoEntity;import com.example.entity.VideoWatched;import com.example.enums.ProfileRole;import com.example.enums.VideoStatus;import com.example.mapper.VideoFullInfoMapper;import com.example.mapper.VideoPlayListInfoMapper;import com.example.mapper.VideoShortInfoMapper;import com.example.repository.VideoRepository;import com.example.repository.VideoTagRepository;import com.example.repository.VideoWatchedRepository;import com.example.util.SpringSecurityUtil;import lombok.extern.slf4j.Slf4j;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.*;import org.springframework.stereotype.Service;import java.util.List;import java.util.Optional;@Service@Slf4jpublic class VideoService {    @Autowired    private VideoRepository videoRepository;    @Autowired    private VideoWatchedRepository videoWatchedRepository;    @Autowired    private ResourceBundleService resourceBundleService;    @Autowired    private VideoTagRepository videoTagRepository;//    private final Logger log = LoggerFactory.getLogger(VideoService.class);    private VideoEntity TO_ENTITY(VideoDTO dto) {        VideoEntity entity = new VideoEntity();        entity.setPreviewAttachId(dto.getPreviewAttachId());        entity.setTitle(dto.getTitle());        entity.setCategoryId(dto.getCategoryId());        entity.setAttachId(dto.getAttachId());        entity.setChannelId(dto.getChannelId());        entity.setVideoType(dto.getVideoType());        entity.setVideoStatus(dto.getVideoStatus());        entity.setDescription(dto.getDescription());        entity.setPrtId(dto.getPrtId());        entity.setVideoTagEntityList(dto.getVideoTagEntityList());        entity.setVideoKind(dto.getVideoKind());        //all counts are default 0        return entity;    }    public VideoDTO  TO_DTO(VideoEntity entity) {        VideoDTO dto = new VideoDTO();        dto.setId(entity.getId());        dto.setVisible(entity.getVisible());        dto.setCreatedDate(entity.getCreatedDate());        dto.setVideoKind(entity.getVideoKind());        dto.setPreviewAttachId(entity.getPreviewAttachId());        dto.setTitle(entity.getTitle());        dto.setCategoryId(entity.getCategoryId());        dto.setAttachId(entity.getAttachId());        dto.setChannelId(entity.getChannelId());        dto.setVideoType(entity.getVideoType());        dto.setVideoStatus(entity.getVideoStatus());        dto.setDescription(entity.getDescription());        return dto;    }    public ApiResponse create(VideoDTO dto) {        Integer prtId = SpringSecurityUtil.getCurrentProfileId();        dto.setPrtId(prtId);        VideoEntity entity = TO_ENTITY(dto);        VideoEntity created = videoRepository.save(entity);        videoTagRepository.saveAll(entity.getVideoTagEntityList());//save video tags        log.info("created video id: " + created.getId() + " title: " + created.getTitle());        return new ApiResponse(true, TO_DTO(entity));    }    public ApiResponse update(String id, VideoDTO dto) {        CustomUserDetails currentUser = SpringSecurityUtil.getCurrentUser();        Optional<VideoEntity> optionalVideo = videoRepository.findById(id);        ProfileEntity profile = currentUser.getProfile();        if (optionalVideo.isPresent()) {            VideoEntity entity = optionalVideo.get();            if (entity.getPrtId().equals(profile.getId())) {//                entity.setPreviewAttachId(dto.getPreviewAttachId());//                entity.setCategoryId(dto.getCategoryId());//                entity.setAttachId(dto.getAttachId());//                entity.setChannelId(dto.getChannelId());//                entity.setVideoType(dto.getVideoType());//                entity.setVideoStatus(dto.getVideoStatus());                entity.setTitle(dto.getTitle());                entity.setDescription(dto.getDescription());                log.warn("updated video details id: " + entity.getId() + " title: " + entity.getTitle());                VideoEntity updated = videoRepository.save(entity);                return new ApiResponse(true, TO_DTO(updated));            } else {                return new ApiResponse(false, resourceBundleService.getMessage("you.are.not.allowed", profile.getLanguage()));            }        }        return new ApiResponse(false, resourceBundleService.getMessage("item.not.found", profile.getLanguage()));    }    public ApiResponse updateStatus(String id) {        Optional<VideoEntity> optionalVideo = videoRepository.findById(id);        ProfileEntity profile = SpringSecurityUtil.getProfileEntity();        if (optionalVideo.isPresent()) {            VideoEntity entity = optionalVideo.get();            if (entity.getPrtId().equals(profile.getId()) ||            profile.getRole().equals(ProfileRole.ROLE_ADMIN)) {                if (entity.getVideoStatus().equals(VideoStatus.PUBLIC)) {                    entity.setVideoStatus(VideoStatus.PRIVATE);                } else {                    entity.setVideoStatus(VideoStatus.PUBLIC);                }                VideoEntity updated = videoRepository.save(entity);                log.warn("updated video status id: " + entity.getId() + " title: " + entity.getTitle() + " status" + entity.getVideoStatus());                return new ApiResponse(true, TO_DTO(updated));            } else {                return new ApiResponse(false, resourceBundleService.getMessage("you.are.not.allowed", SpringSecurityUtil.getUserLang()));            }        }        return new ApiResponse(false, resourceBundleService.getMessage("item.not.found", SpringSecurityUtil.getUserLang()));    }    public ApiResponse increaseViewCount(String id) {        Optional<VideoEntity> optionalVideo = videoRepository.findById(id);        Integer currentUserId = SpringSecurityUtil.getCurrentProfileId();        if (optionalVideo.isPresent()) {            VideoEntity entity = optionalVideo.get();            VideoWatched videoWatched = new VideoWatched();            videoWatched.setVideoId(entity.getId());            videoWatched.setProfileId(currentUserId);            videoWatchedRepository.save(videoWatched);//trigger worked after insert video_watched            return new ApiResponse(true, videoWatched);        }        return new ApiResponse(false, resourceBundleService.getMessage("update.view.count.failed", SpringSecurityUtil.getUserLang()));    }    public Page<VideoDTO> pagingByCategoryId(Integer categoryId, Integer page, Integer size) {        Sort sort = Sort.by("viewCount");        Pageable pageable = PageRequest.of(page, size, sort);        Page<VideoEntity> videoEntityPage = videoRepository                .findAllByCategoryId(categoryId, pageable);        List<VideoDTO> videoDTOList = videoEntityPage                .stream()                .map(this::TO_DTO)                .toList();        return new PageImpl<>(videoDTOList, pageable, videoEntityPage.getTotalPages());    }    public Page<VideoDTO> searchVideoByTitle(String title, Integer page, Integer size) {        Sort sort = Sort.by("view_count");        Pageable pageable = PageRequest.of(page, size, sort);        Page<VideoEntity> videoEntityPage = videoRepository                .findAllByTitle("%" + title + "%", pageable);        List<VideoDTO> videoDTOList = videoEntityPage                .stream()                .map(this::TO_DTO)                .toList();        return new PageImpl<>(videoDTOList, pageable, videoEntityPage.getTotalPages());    }    public Page<VideoDTO> pagingByTagId(Integer id, Integer page, Integer size) {        Sort sort = Sort.by("view_count");        Pageable pageable = PageRequest.of(page, size, sort);        Page<VideoEntity> videoEntityPage = videoRepository                .findAllByTagId(id, pageable);        List<VideoDTO> videoDTOList = videoEntityPage                .stream()                .map(this::TO_DTO)                .toList();        return new PageImpl<>(videoDTOList, pageable, videoEntityPage.getTotalPages());    }    public ApiResponse searchVideoById(String id) {        ProfileEntity currentUser = SpringSecurityUtil.getProfileEntity();        Optional<VideoEntity> optionalVideo = videoRepository.findById(id);        if (optionalVideo.isPresent()) {            VideoEntity videoEntity = optionalVideo.get();            if (videoEntity.getVisible() &&            !videoEntity.getVideoStatus().equals(VideoStatus.PRIVATE)) {                return new ApiResponse(true, videoEntity); // FULL INFO            } else if (videoEntity.getPrtId().equals(currentUser.getId()) ||                    currentUser.getRole().equals(ProfileRole.ROLE_ADMIN)) {                return new ApiResponse(true, videoEntity); // FULL INFO            } else{                return new ApiResponse(false, resourceBundleService.getMessage("you.are.not.allowed", SpringSecurityUtil.getUserLang()));            }        }        return new ApiResponse(false, resourceBundleService.getMessage("item.not.found", SpringSecurityUtil.getUserLang()));    }    public Page<VideoDTO> pagingByCreatedDateDescending(Integer page, Integer size) {        Sort sort = Sort.by("createdDate").descending();        Pageable pageable = PageRequest.of(page, size, sort);        Page<VideoEntity> videoEntityPage = videoRepository                .findAllByCreatedDate(pageable);        List<VideoDTO> videoProfileDTOList = videoEntityPage                .stream()                .map(this::TO_DTO)                .toList();        log.info("paging video by created date");        return new PageImpl<>(videoProfileDTOList, pageable, videoEntityPage.getTotalPages());    }    public Page<VideoShortInfoMapper> pagingVideoShortInfo(Integer page, Integer size) {        Sort sort = Sort.by("created_date").descending();        Pageable pageable = PageRequest.of(page, size, sort);        log.info("paging video short info");        return videoRepository                .pagingVideoShortInfo(pageable);    }    public Page<VideoFullInfoMapper> pagingVideoFullInfo(Integer page, Integer size) {        Sort sort = Sort.by("created_date").descending();        Pageable pageable = PageRequest.of(page, size, sort);        log.info("paging video full info");        return videoRepository                .pagingVideoFullInfo(pageable);    }    public Page<VideoPlayListInfoMapper> pagingVideoPlaylistInfo(Integer page, Integer size) {        Sort sort = Sort.by("created_date").ascending();        Pageable pageable = PageRequest.of(page, size, sort);        log.info("paging video playlist info");        return videoRepository                .pagingVideoPlaylistInfo(pageable);    }}