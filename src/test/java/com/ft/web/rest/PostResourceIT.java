package com.ft.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ft.IntegrationTest;
import com.ft.domain.Category;
import com.ft.domain.Post;
import com.ft.repository.PostRepository;
import com.ft.repository.search.PostSearchRepository;
import com.ft.service.criteria.PostCriteria;
import com.ft.service.dto.PostDTO;
import com.ft.service.mapper.PostMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

/**
 * Integration tests for the {@link PostResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class PostResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_SLUG = "AAAAAAAAAA";
    private static final String UPDATED_SLUG = "BBBBBBBBBB";

    private static final String DEFAULT_SUMMARY = "AAAAAAAAAA";
    private static final String UPDATED_SUMMARY = "BBBBBBBBBB";

    private static final String DEFAULT_BODY = "AAAAAAAAAA";
    private static final String UPDATED_BODY = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_CREATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_CREATED_BY = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_PUBLISHED_DATE = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_PUBLISHED_DATE = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_PUBLISHED_DATE = LocalDate.ofEpochDay(-1L);

    private static final Integer DEFAULT_STATE = 1;
    private static final Integer UPDATED_STATE = 2;
    private static final Integer SMALLER_STATE = 1 - 1;

    private static final String DEFAULT_TAGS = "AAAAAAAAAA";
    private static final String UPDATED_TAGS = "BBBBBBBBBB";

    private static final Instant DEFAULT_UPDATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_UPDATED_AT = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String DEFAULT_UPDATED_BY = "AAAAAAAAAA";
    private static final String UPDATED_UPDATED_BY = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/posts";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/posts";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMapper postMapper;

    /**
     * This repository is mocked in the com.ft.repository.search test package.
     *
     * @see com.ft.repository.search.PostSearchRepositoryMockConfiguration
     */
    @Autowired
    private PostSearchRepository mockPostSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restPostMockMvc;

    private Post post;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createEntity(EntityManager em) {
        Post post = new Post()
            .title(DEFAULT_TITLE)
            .slug(DEFAULT_SLUG)
            .summary(DEFAULT_SUMMARY)
            .body(DEFAULT_BODY)
            .createdAt(DEFAULT_CREATED_AT)
            .createdBy(DEFAULT_CREATED_BY)
            .publishedDate(DEFAULT_PUBLISHED_DATE)
            .state(DEFAULT_STATE)
            .tags(DEFAULT_TAGS)
            .updatedAt(DEFAULT_UPDATED_AT)
            .updatedBy(DEFAULT_UPDATED_BY);
        return post;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Post createUpdatedEntity(EntityManager em) {
        Post post = new Post()
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .summary(UPDATED_SUMMARY)
            .body(UPDATED_BODY)
            .createdAt(UPDATED_CREATED_AT)
            .createdBy(UPDATED_CREATED_BY)
            .publishedDate(UPDATED_PUBLISHED_DATE)
            .state(UPDATED_STATE)
            .tags(UPDATED_TAGS)
            .updatedAt(UPDATED_UPDATED_AT)
            .updatedBy(UPDATED_UPDATED_BY);
        return post;
    }

    @BeforeEach
    public void initTest() {
        post = createEntity(em);
    }

    @Test
    @Transactional
    void createPost() throws Exception {
        int databaseSizeBeforeCreate = postRepository.findAll().size();
        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);
        restPostMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isCreated());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeCreate + 1);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPost.getSummary()).isEqualTo(DEFAULT_SUMMARY);
        assertThat(testPost.getBody()).isEqualTo(DEFAULT_BODY);
        assertThat(testPost.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testPost.getCreatedBy()).isEqualTo(DEFAULT_CREATED_BY);
        assertThat(testPost.getPublishedDate()).isEqualTo(DEFAULT_PUBLISHED_DATE);
        assertThat(testPost.getState()).isEqualTo(DEFAULT_STATE);
        assertThat(testPost.getTags()).isEqualTo(DEFAULT_TAGS);
        assertThat(testPost.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testPost.getUpdatedBy()).isEqualTo(DEFAULT_UPDATED_BY);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(1)).save(testPost);
    }

    @Test
    @Transactional
    void createPostWithExistingId() throws Exception {
        // Create the Post with an existing ID
        post.setId(1L);
        PostDTO postDTO = postMapper.toDto(post);

        int databaseSizeBeforeCreate = postRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restPostMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeCreate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = postRepository.findAll().size();
        // set the field null
        post.setTitle(null);

        // Create the Post, which fails.
        PostDTO postDTO = postMapper.toDto(post);

        restPostMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isBadRequest());

        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllPosts() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList
        restPostMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(post.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].publishedDate").value(hasItem(DEFAULT_PUBLISHED_DATE.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedBy").value(hasItem(DEFAULT_UPDATED_BY)));
    }

    @Test
    @Transactional
    void getPost() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get the post
        restPostMockMvc
            .perform(get(ENTITY_API_URL_ID, post.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(post.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.slug").value(DEFAULT_SLUG))
            .andExpect(jsonPath("$.summary").value(DEFAULT_SUMMARY))
            .andExpect(jsonPath("$.body").value(DEFAULT_BODY.toString()))
            .andExpect(jsonPath("$.createdAt").value(DEFAULT_CREATED_AT.toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_CREATED_BY))
            .andExpect(jsonPath("$.publishedDate").value(DEFAULT_PUBLISHED_DATE.toString()))
            .andExpect(jsonPath("$.state").value(DEFAULT_STATE))
            .andExpect(jsonPath("$.tags").value(DEFAULT_TAGS))
            .andExpect(jsonPath("$.updatedAt").value(DEFAULT_UPDATED_AT.toString()))
            .andExpect(jsonPath("$.updatedBy").value(DEFAULT_UPDATED_BY));
    }

    @Test
    @Transactional
    void getPostsByIdFiltering() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        Long id = post.getId();

        defaultPostShouldBeFound("id.equals=" + id);
        defaultPostShouldNotBeFound("id.notEquals=" + id);

        defaultPostShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultPostShouldNotBeFound("id.greaterThan=" + id);

        defaultPostShouldBeFound("id.lessThanOrEqual=" + id);
        defaultPostShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllPostsByTitleIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title equals to DEFAULT_TITLE
        defaultPostShouldBeFound("title.equals=" + DEFAULT_TITLE);

        // Get all the postList where title equals to UPDATED_TITLE
        defaultPostShouldNotBeFound("title.equals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllPostsByTitleIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title not equals to DEFAULT_TITLE
        defaultPostShouldNotBeFound("title.notEquals=" + DEFAULT_TITLE);

        // Get all the postList where title not equals to UPDATED_TITLE
        defaultPostShouldBeFound("title.notEquals=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllPostsByTitleIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title in DEFAULT_TITLE or UPDATED_TITLE
        defaultPostShouldBeFound("title.in=" + DEFAULT_TITLE + "," + UPDATED_TITLE);

        // Get all the postList where title equals to UPDATED_TITLE
        defaultPostShouldNotBeFound("title.in=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllPostsByTitleIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title is not null
        defaultPostShouldBeFound("title.specified=true");

        // Get all the postList where title is null
        defaultPostShouldNotBeFound("title.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByTitleContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title contains DEFAULT_TITLE
        defaultPostShouldBeFound("title.contains=" + DEFAULT_TITLE);

        // Get all the postList where title contains UPDATED_TITLE
        defaultPostShouldNotBeFound("title.contains=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllPostsByTitleNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where title does not contain DEFAULT_TITLE
        defaultPostShouldNotBeFound("title.doesNotContain=" + DEFAULT_TITLE);

        // Get all the postList where title does not contain UPDATED_TITLE
        defaultPostShouldBeFound("title.doesNotContain=" + UPDATED_TITLE);
    }

    @Test
    @Transactional
    void getAllPostsBySlugIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug equals to DEFAULT_SLUG
        defaultPostShouldBeFound("slug.equals=" + DEFAULT_SLUG);

        // Get all the postList where slug equals to UPDATED_SLUG
        defaultPostShouldNotBeFound("slug.equals=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllPostsBySlugIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug not equals to DEFAULT_SLUG
        defaultPostShouldNotBeFound("slug.notEquals=" + DEFAULT_SLUG);

        // Get all the postList where slug not equals to UPDATED_SLUG
        defaultPostShouldBeFound("slug.notEquals=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllPostsBySlugIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug in DEFAULT_SLUG or UPDATED_SLUG
        defaultPostShouldBeFound("slug.in=" + DEFAULT_SLUG + "," + UPDATED_SLUG);

        // Get all the postList where slug equals to UPDATED_SLUG
        defaultPostShouldNotBeFound("slug.in=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllPostsBySlugIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug is not null
        defaultPostShouldBeFound("slug.specified=true");

        // Get all the postList where slug is null
        defaultPostShouldNotBeFound("slug.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsBySlugContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug contains DEFAULT_SLUG
        defaultPostShouldBeFound("slug.contains=" + DEFAULT_SLUG);

        // Get all the postList where slug contains UPDATED_SLUG
        defaultPostShouldNotBeFound("slug.contains=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllPostsBySlugNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where slug does not contain DEFAULT_SLUG
        defaultPostShouldNotBeFound("slug.doesNotContain=" + DEFAULT_SLUG);

        // Get all the postList where slug does not contain UPDATED_SLUG
        defaultPostShouldBeFound("slug.doesNotContain=" + UPDATED_SLUG);
    }

    @Test
    @Transactional
    void getAllPostsBySummaryIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary equals to DEFAULT_SUMMARY
        defaultPostShouldBeFound("summary.equals=" + DEFAULT_SUMMARY);

        // Get all the postList where summary equals to UPDATED_SUMMARY
        defaultPostShouldNotBeFound("summary.equals=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllPostsBySummaryIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary not equals to DEFAULT_SUMMARY
        defaultPostShouldNotBeFound("summary.notEquals=" + DEFAULT_SUMMARY);

        // Get all the postList where summary not equals to UPDATED_SUMMARY
        defaultPostShouldBeFound("summary.notEquals=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllPostsBySummaryIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary in DEFAULT_SUMMARY or UPDATED_SUMMARY
        defaultPostShouldBeFound("summary.in=" + DEFAULT_SUMMARY + "," + UPDATED_SUMMARY);

        // Get all the postList where summary equals to UPDATED_SUMMARY
        defaultPostShouldNotBeFound("summary.in=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllPostsBySummaryIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary is not null
        defaultPostShouldBeFound("summary.specified=true");

        // Get all the postList where summary is null
        defaultPostShouldNotBeFound("summary.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsBySummaryContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary contains DEFAULT_SUMMARY
        defaultPostShouldBeFound("summary.contains=" + DEFAULT_SUMMARY);

        // Get all the postList where summary contains UPDATED_SUMMARY
        defaultPostShouldNotBeFound("summary.contains=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllPostsBySummaryNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where summary does not contain DEFAULT_SUMMARY
        defaultPostShouldNotBeFound("summary.doesNotContain=" + DEFAULT_SUMMARY);

        // Get all the postList where summary does not contain UPDATED_SUMMARY
        defaultPostShouldBeFound("summary.doesNotContain=" + UPDATED_SUMMARY);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdAt equals to DEFAULT_CREATED_AT
        defaultPostShouldBeFound("createdAt.equals=" + DEFAULT_CREATED_AT);

        // Get all the postList where createdAt equals to UPDATED_CREATED_AT
        defaultPostShouldNotBeFound("createdAt.equals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdAt not equals to DEFAULT_CREATED_AT
        defaultPostShouldNotBeFound("createdAt.notEquals=" + DEFAULT_CREATED_AT);

        // Get all the postList where createdAt not equals to UPDATED_CREATED_AT
        defaultPostShouldBeFound("createdAt.notEquals=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdAt in DEFAULT_CREATED_AT or UPDATED_CREATED_AT
        defaultPostShouldBeFound("createdAt.in=" + DEFAULT_CREATED_AT + "," + UPDATED_CREATED_AT);

        // Get all the postList where createdAt equals to UPDATED_CREATED_AT
        defaultPostShouldNotBeFound("createdAt.in=" + UPDATED_CREATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdAt is not null
        defaultPostShouldBeFound("createdAt.specified=true");

        // Get all the postList where createdAt is null
        defaultPostShouldNotBeFound("createdAt.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy equals to DEFAULT_CREATED_BY
        defaultPostShouldBeFound("createdBy.equals=" + DEFAULT_CREATED_BY);

        // Get all the postList where createdBy equals to UPDATED_CREATED_BY
        defaultPostShouldNotBeFound("createdBy.equals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy not equals to DEFAULT_CREATED_BY
        defaultPostShouldNotBeFound("createdBy.notEquals=" + DEFAULT_CREATED_BY);

        // Get all the postList where createdBy not equals to UPDATED_CREATED_BY
        defaultPostShouldBeFound("createdBy.notEquals=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy in DEFAULT_CREATED_BY or UPDATED_CREATED_BY
        defaultPostShouldBeFound("createdBy.in=" + DEFAULT_CREATED_BY + "," + UPDATED_CREATED_BY);

        // Get all the postList where createdBy equals to UPDATED_CREATED_BY
        defaultPostShouldNotBeFound("createdBy.in=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy is not null
        defaultPostShouldBeFound("createdBy.specified=true");

        // Get all the postList where createdBy is null
        defaultPostShouldNotBeFound("createdBy.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy contains DEFAULT_CREATED_BY
        defaultPostShouldBeFound("createdBy.contains=" + DEFAULT_CREATED_BY);

        // Get all the postList where createdBy contains UPDATED_CREATED_BY
        defaultPostShouldNotBeFound("createdBy.contains=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByCreatedByNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where createdBy does not contain DEFAULT_CREATED_BY
        defaultPostShouldNotBeFound("createdBy.doesNotContain=" + DEFAULT_CREATED_BY);

        // Get all the postList where createdBy does not contain UPDATED_CREATED_BY
        defaultPostShouldBeFound("createdBy.doesNotContain=" + UPDATED_CREATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate equals to DEFAULT_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.equals=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate equals to UPDATED_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.equals=" + UPDATED_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate not equals to DEFAULT_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.notEquals=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate not equals to UPDATED_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.notEquals=" + UPDATED_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate in DEFAULT_PUBLISHED_DATE or UPDATED_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.in=" + DEFAULT_PUBLISHED_DATE + "," + UPDATED_PUBLISHED_DATE);

        // Get all the postList where publishedDate equals to UPDATED_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.in=" + UPDATED_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate is not null
        defaultPostShouldBeFound("publishedDate.specified=true");

        // Get all the postList where publishedDate is null
        defaultPostShouldNotBeFound("publishedDate.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate is greater than or equal to DEFAULT_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.greaterThanOrEqual=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate is greater than or equal to UPDATED_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.greaterThanOrEqual=" + UPDATED_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate is less than or equal to DEFAULT_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.lessThanOrEqual=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate is less than or equal to SMALLER_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.lessThanOrEqual=" + SMALLER_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsLessThanSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate is less than DEFAULT_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.lessThan=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate is less than UPDATED_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.lessThan=" + UPDATED_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByPublishedDateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where publishedDate is greater than DEFAULT_PUBLISHED_DATE
        defaultPostShouldNotBeFound("publishedDate.greaterThan=" + DEFAULT_PUBLISHED_DATE);

        // Get all the postList where publishedDate is greater than SMALLER_PUBLISHED_DATE
        defaultPostShouldBeFound("publishedDate.greaterThan=" + SMALLER_PUBLISHED_DATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state equals to DEFAULT_STATE
        defaultPostShouldBeFound("state.equals=" + DEFAULT_STATE);

        // Get all the postList where state equals to UPDATED_STATE
        defaultPostShouldNotBeFound("state.equals=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state not equals to DEFAULT_STATE
        defaultPostShouldNotBeFound("state.notEquals=" + DEFAULT_STATE);

        // Get all the postList where state not equals to UPDATED_STATE
        defaultPostShouldBeFound("state.notEquals=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state in DEFAULT_STATE or UPDATED_STATE
        defaultPostShouldBeFound("state.in=" + DEFAULT_STATE + "," + UPDATED_STATE);

        // Get all the postList where state equals to UPDATED_STATE
        defaultPostShouldNotBeFound("state.in=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state is not null
        defaultPostShouldBeFound("state.specified=true");

        // Get all the postList where state is null
        defaultPostShouldNotBeFound("state.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByStateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state is greater than or equal to DEFAULT_STATE
        defaultPostShouldBeFound("state.greaterThanOrEqual=" + DEFAULT_STATE);

        // Get all the postList where state is greater than or equal to UPDATED_STATE
        defaultPostShouldNotBeFound("state.greaterThanOrEqual=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state is less than or equal to DEFAULT_STATE
        defaultPostShouldBeFound("state.lessThanOrEqual=" + DEFAULT_STATE);

        // Get all the postList where state is less than or equal to SMALLER_STATE
        defaultPostShouldNotBeFound("state.lessThanOrEqual=" + SMALLER_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsLessThanSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state is less than DEFAULT_STATE
        defaultPostShouldNotBeFound("state.lessThan=" + DEFAULT_STATE);

        // Get all the postList where state is less than UPDATED_STATE
        defaultPostShouldBeFound("state.lessThan=" + UPDATED_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByStateIsGreaterThanSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where state is greater than DEFAULT_STATE
        defaultPostShouldNotBeFound("state.greaterThan=" + DEFAULT_STATE);

        // Get all the postList where state is greater than SMALLER_STATE
        defaultPostShouldBeFound("state.greaterThan=" + SMALLER_STATE);
    }

    @Test
    @Transactional
    void getAllPostsByTagsIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags equals to DEFAULT_TAGS
        defaultPostShouldBeFound("tags.equals=" + DEFAULT_TAGS);

        // Get all the postList where tags equals to UPDATED_TAGS
        defaultPostShouldNotBeFound("tags.equals=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllPostsByTagsIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags not equals to DEFAULT_TAGS
        defaultPostShouldNotBeFound("tags.notEquals=" + DEFAULT_TAGS);

        // Get all the postList where tags not equals to UPDATED_TAGS
        defaultPostShouldBeFound("tags.notEquals=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllPostsByTagsIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags in DEFAULT_TAGS or UPDATED_TAGS
        defaultPostShouldBeFound("tags.in=" + DEFAULT_TAGS + "," + UPDATED_TAGS);

        // Get all the postList where tags equals to UPDATED_TAGS
        defaultPostShouldNotBeFound("tags.in=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllPostsByTagsIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags is not null
        defaultPostShouldBeFound("tags.specified=true");

        // Get all the postList where tags is null
        defaultPostShouldNotBeFound("tags.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByTagsContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags contains DEFAULT_TAGS
        defaultPostShouldBeFound("tags.contains=" + DEFAULT_TAGS);

        // Get all the postList where tags contains UPDATED_TAGS
        defaultPostShouldNotBeFound("tags.contains=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllPostsByTagsNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where tags does not contain DEFAULT_TAGS
        defaultPostShouldNotBeFound("tags.doesNotContain=" + DEFAULT_TAGS);

        // Get all the postList where tags does not contain UPDATED_TAGS
        defaultPostShouldBeFound("tags.doesNotContain=" + UPDATED_TAGS);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedAt equals to DEFAULT_UPDATED_AT
        defaultPostShouldBeFound("updatedAt.equals=" + DEFAULT_UPDATED_AT);

        // Get all the postList where updatedAt equals to UPDATED_UPDATED_AT
        defaultPostShouldNotBeFound("updatedAt.equals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedAt not equals to DEFAULT_UPDATED_AT
        defaultPostShouldNotBeFound("updatedAt.notEquals=" + DEFAULT_UPDATED_AT);

        // Get all the postList where updatedAt not equals to UPDATED_UPDATED_AT
        defaultPostShouldBeFound("updatedAt.notEquals=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedAt in DEFAULT_UPDATED_AT or UPDATED_UPDATED_AT
        defaultPostShouldBeFound("updatedAt.in=" + DEFAULT_UPDATED_AT + "," + UPDATED_UPDATED_AT);

        // Get all the postList where updatedAt equals to UPDATED_UPDATED_AT
        defaultPostShouldNotBeFound("updatedAt.in=" + UPDATED_UPDATED_AT);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedAtIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedAt is not null
        defaultPostShouldBeFound("updatedAt.specified=true");

        // Get all the postList where updatedAt is null
        defaultPostShouldNotBeFound("updatedAt.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy equals to DEFAULT_UPDATED_BY
        defaultPostShouldBeFound("updatedBy.equals=" + DEFAULT_UPDATED_BY);

        // Get all the postList where updatedBy equals to UPDATED_UPDATED_BY
        defaultPostShouldNotBeFound("updatedBy.equals=" + UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy not equals to DEFAULT_UPDATED_BY
        defaultPostShouldNotBeFound("updatedBy.notEquals=" + DEFAULT_UPDATED_BY);

        // Get all the postList where updatedBy not equals to UPDATED_UPDATED_BY
        defaultPostShouldBeFound("updatedBy.notEquals=" + UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByIsInShouldWork() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy in DEFAULT_UPDATED_BY or UPDATED_UPDATED_BY
        defaultPostShouldBeFound("updatedBy.in=" + DEFAULT_UPDATED_BY + "," + UPDATED_UPDATED_BY);

        // Get all the postList where updatedBy equals to UPDATED_UPDATED_BY
        defaultPostShouldNotBeFound("updatedBy.in=" + UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByIsNullOrNotNull() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy is not null
        defaultPostShouldBeFound("updatedBy.specified=true");

        // Get all the postList where updatedBy is null
        defaultPostShouldNotBeFound("updatedBy.specified=false");
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy contains DEFAULT_UPDATED_BY
        defaultPostShouldBeFound("updatedBy.contains=" + DEFAULT_UPDATED_BY);

        // Get all the postList where updatedBy contains UPDATED_UPDATED_BY
        defaultPostShouldNotBeFound("updatedBy.contains=" + UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByUpdatedByNotContainsSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        // Get all the postList where updatedBy does not contain DEFAULT_UPDATED_BY
        defaultPostShouldNotBeFound("updatedBy.doesNotContain=" + DEFAULT_UPDATED_BY);

        // Get all the postList where updatedBy does not contain UPDATED_UPDATED_BY
        defaultPostShouldBeFound("updatedBy.doesNotContain=" + UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void getAllPostsByCategoryIsEqualToSomething() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);
        Category category = CategoryResourceIT.createEntity(em);
        em.persist(category);
        em.flush();
        post.setCategory(category);
        postRepository.saveAndFlush(post);
        Long categoryId = category.getId();

        // Get all the postList where category equals to categoryId
        defaultPostShouldBeFound("categoryId.equals=" + categoryId);

        // Get all the postList where category equals to (categoryId + 1)
        defaultPostShouldNotBeFound("categoryId.equals=" + (categoryId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultPostShouldBeFound(String filter) throws Exception {
        restPostMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(post.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].publishedDate").value(hasItem(DEFAULT_PUBLISHED_DATE.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedBy").value(hasItem(DEFAULT_UPDATED_BY)));

        // Check, that the count call also returns 1
        restPostMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultPostShouldNotBeFound(String filter) throws Exception {
        restPostMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restPostMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingPost() throws Exception {
        // Get the post
        restPostMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewPost() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeUpdate = postRepository.findAll().size();

        // Update the post
        Post updatedPost = postRepository.findById(post.getId()).get();
        // Disconnect from session so that the updates on updatedPost are not directly saved in db
        em.detach(updatedPost);
        updatedPost
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .summary(UPDATED_SUMMARY)
            .body(UPDATED_BODY)
            .createdAt(UPDATED_CREATED_AT)
            .createdBy(UPDATED_CREATED_BY)
            .publishedDate(UPDATED_PUBLISHED_DATE)
            .state(UPDATED_STATE)
            .tags(UPDATED_TAGS)
            .updatedAt(UPDATED_UPDATED_AT)
            .updatedBy(UPDATED_UPDATED_BY);
        PostDTO postDTO = postMapper.toDto(updatedPost);

        restPostMockMvc
            .perform(
                put(ENTITY_API_URL_ID, postDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(postDTO))
            )
            .andExpect(status().isOk());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPost.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(testPost.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testPost.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPost.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testPost.getPublishedDate()).isEqualTo(UPDATED_PUBLISHED_DATE);
        assertThat(testPost.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testPost.getTags()).isEqualTo(UPDATED_TAGS);
        assertThat(testPost.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPost.getUpdatedBy()).isEqualTo(UPDATED_UPDATED_BY);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository).save(testPost);
    }

    @Test
    @Transactional
    void putNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(
                put(ENTITY_API_URL_ID, postDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(postDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void putWithIdMismatchPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(postDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void partialUpdatePostWithPatch() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeUpdate = postRepository.findAll().size();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        partialUpdatedPost.body(UPDATED_BODY).createdBy(UPDATED_CREATED_BY).publishedDate(UPDATED_PUBLISHED_DATE).state(UPDATED_STATE);

        restPostMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPost.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPost))
            )
            .andExpect(status().isOk());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPost.getSlug()).isEqualTo(DEFAULT_SLUG);
        assertThat(testPost.getSummary()).isEqualTo(DEFAULT_SUMMARY);
        assertThat(testPost.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testPost.getCreatedAt()).isEqualTo(DEFAULT_CREATED_AT);
        assertThat(testPost.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testPost.getPublishedDate()).isEqualTo(UPDATED_PUBLISHED_DATE);
        assertThat(testPost.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testPost.getTags()).isEqualTo(DEFAULT_TAGS);
        assertThat(testPost.getUpdatedAt()).isEqualTo(DEFAULT_UPDATED_AT);
        assertThat(testPost.getUpdatedBy()).isEqualTo(DEFAULT_UPDATED_BY);
    }

    @Test
    @Transactional
    void fullUpdatePostWithPatch() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeUpdate = postRepository.findAll().size();

        // Update the post using partial update
        Post partialUpdatedPost = new Post();
        partialUpdatedPost.setId(post.getId());

        partialUpdatedPost
            .title(UPDATED_TITLE)
            .slug(UPDATED_SLUG)
            .summary(UPDATED_SUMMARY)
            .body(UPDATED_BODY)
            .createdAt(UPDATED_CREATED_AT)
            .createdBy(UPDATED_CREATED_BY)
            .publishedDate(UPDATED_PUBLISHED_DATE)
            .state(UPDATED_STATE)
            .tags(UPDATED_TAGS)
            .updatedAt(UPDATED_UPDATED_AT)
            .updatedBy(UPDATED_UPDATED_BY);

        restPostMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedPost.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedPost))
            )
            .andExpect(status().isOk());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);
        Post testPost = postList.get(postList.size() - 1);
        assertThat(testPost.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPost.getSlug()).isEqualTo(UPDATED_SLUG);
        assertThat(testPost.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(testPost.getBody()).isEqualTo(UPDATED_BODY);
        assertThat(testPost.getCreatedAt()).isEqualTo(UPDATED_CREATED_AT);
        assertThat(testPost.getCreatedBy()).isEqualTo(UPDATED_CREATED_BY);
        assertThat(testPost.getPublishedDate()).isEqualTo(UPDATED_PUBLISHED_DATE);
        assertThat(testPost.getState()).isEqualTo(UPDATED_STATE);
        assertThat(testPost.getTags()).isEqualTo(UPDATED_TAGS);
        assertThat(testPost.getUpdatedAt()).isEqualTo(UPDATED_UPDATED_AT);
        assertThat(testPost.getUpdatedBy()).isEqualTo(UPDATED_UPDATED_BY);
    }

    @Test
    @Transactional
    void patchNonExistingPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, postDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(postDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void patchWithIdMismatchPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(postDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamPost() throws Exception {
        int databaseSizeBeforeUpdate = postRepository.findAll().size();
        post.setId(count.incrementAndGet());

        // Create the Post
        PostDTO postDTO = postMapper.toDto(post);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restPostMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(postDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Post in the database
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(0)).save(post);
    }

    @Test
    @Transactional
    void deletePost() throws Exception {
        // Initialize the database
        postRepository.saveAndFlush(post);

        int databaseSizeBeforeDelete = postRepository.findAll().size();

        // Delete the post
        restPostMockMvc
            .perform(delete(ENTITY_API_URL_ID, post.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Post> postList = postRepository.findAll();
        assertThat(postList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Post in Elasticsearch
        verify(mockPostSearchRepository, times(1)).deleteById(post.getId());
    }

    @Test
    @Transactional
    void searchPost() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        postRepository.saveAndFlush(post);
        when(mockPostSearchRepository.search(queryStringQuery("id:" + post.getId()), PageRequest.of(0, 20)))
            .thenReturn(new PageImpl<>(Collections.singletonList(post), PageRequest.of(0, 1), 1));

        // Search the post
        restPostMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + post.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(post.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].slug").value(hasItem(DEFAULT_SLUG)))
            .andExpect(jsonPath("$.[*].summary").value(hasItem(DEFAULT_SUMMARY)))
            .andExpect(jsonPath("$.[*].body").value(hasItem(DEFAULT_BODY.toString())))
            .andExpect(jsonPath("$.[*].createdAt").value(hasItem(DEFAULT_CREATED_AT.toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_CREATED_BY)))
            .andExpect(jsonPath("$.[*].publishedDate").value(hasItem(DEFAULT_PUBLISHED_DATE.toString())))
            .andExpect(jsonPath("$.[*].state").value(hasItem(DEFAULT_STATE)))
            .andExpect(jsonPath("$.[*].tags").value(hasItem(DEFAULT_TAGS)))
            .andExpect(jsonPath("$.[*].updatedAt").value(hasItem(DEFAULT_UPDATED_AT.toString())))
            .andExpect(jsonPath("$.[*].updatedBy").value(hasItem(DEFAULT_UPDATED_BY)));
    }
}
