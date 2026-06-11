package com.example.pets_backend.service;

import com.example.pets_backend.dao.QuestionBankDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.TrainingMaterialDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.QuestionBankDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.TrainingMaterialDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.MaterialProgressReqDTO;
import com.example.pets_backend.dto.req.SubmitExamReqDTO;
import com.example.pets_backend.dto.resp.ExamQuestionDTO;
import com.example.pets_backend.dto.resp.StartExamRespDTO;
import com.example.pets_backend.dto.resp.SubmitExamRespDTO;
import com.example.pets_backend.dto.resp.TempCaretakerReadyRespDTO;
import com.example.pets_backend.dto.resp.TrainingCurriculumRespDTO;
import com.example.pets_backend.dto.resp.TrainingMaterialItemRespDTO;
import com.example.pets_backend.dto.resp.TrainingMaterialRespDTO;
import com.example.pets_backend.dto.resp.TrainingStatusRespDTO;
import com.example.pets_backend.enums.UserRoleTypeEnum;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private static final int ROLE_OWNER = 1;
    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int VERIFY_STATUS_INIT = 0;
    private static final int VERIFY_STATUS_LEARNING = 1;
    private static final int VERIFY_STATUS_PASSED = 2;
    private static final int QUESTION_TYPE_BASIC = 1;
    private static final int QUESTION_TYPE_CORE = 2;
    private static final int BASIC_COUNT = 15;
    private static final int CORE_COUNT = 5;
    private static final int TOTAL_QUESTION_COUNT = BASIC_COUNT + CORE_COUNT;
    private static final int SCORE_PER_QUESTION = 5;
    private static final int PASS_SCORE = 90;
    private static final int DEFAULT_CREDIT_SCORE = 80;
    private static final int DEFAULT_SERVICE_RADIUS_KM = 5;
    private static final BigDecimal TEMP_READY_DEPOSIT_AMOUNT = new BigDecimal("500.00");

    private final UserDao userDao;
    private final SitterProfileDao sitterProfileDao;
    private final SitterTrainingRecordDao sitterTrainingRecordDao;
    private final TrainingMaterialDao trainingMaterialDao;
    private final QuestionBankDao questionBankDao;
    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    public TrainingStatusRespDTO getStatus() {
        Long userId = currentUserId();
        UserDO user = requireUser(userId);
        if (!isCaretakerRole(UserContext.getRoleType())) {
            return new TrainingStatusRespDTO(
                    VERIFY_STATUS_INIT,
                    isRealNameVerified(user),
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    0,
                    0);
        }
        SitterProfileDO profile = ensureProfile(userId);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        CurriculumProgress progress = buildCurriculumProgress(record);
        return new TrainingStatusRespDTO(
                profile.getVerifyStatus(),
                isRealNameVerified(user),
                record.getLearningCompletedAt(),
                record.getLastExamScore(),
                record.getLastExamPassed() == null ? null : record.getLastExamPassed() == 1,
                record.getLastExamAt(),
                record.getResetReason(),
                progress.requiredCount(),
                progress.completedCount(),
                progress.progressPercent());
    }

    public TrainingCurriculumRespDTO getCurriculum() {
        Long userId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(userId);
        requireRealNameVerified(user);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        markLearningStarted(record);
        CurriculumProgress progress = buildCurriculumProgress(record);
        return new TrainingCurriculumRespDTO(
                progress.requiredCount(),
                progress.completedCount(),
                progress.progressPercent(),
                progress.materials());
    }

    public TrainingMaterialRespDTO startLearning() {
        TrainingCurriculumRespDTO curriculum = getCurriculum();
        if (curriculum.materials() == null || curriculum.materials().isEmpty()) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
        TrainingMaterialItemRespDTO first = curriculum.materials().get(0);
        return new TrainingMaterialRespDTO(first.materialId(), first.title(), first.content());
    }

    @Transactional
    public TrainingCurriculumRespDTO recordMaterialProgress(Long materialId, MaterialProgressReqDTO reqDTO) {
        if (materialId == null || reqDTO == null || reqDTO.watchedSeconds() == null || reqDTO.watchedSeconds() < 0) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(userId);
        requireRealNameVerified(user);
        TrainingMaterialDO material = requireRequiredMaterial(materialId);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        markLearningStarted(record);

        ObjectNode progressRoot = readProgressRoot(record.getLearningProgressJson());
        ObjectNode item = progressRoot.has(String.valueOf(materialId))
                ? (ObjectNode) progressRoot.get(String.valueOf(materialId))
                : objectMapper.createObjectNode();
        int previousWatched = item.has("watchedSeconds") ? item.get("watchedSeconds").asInt(0) : 0;
        int watchedSeconds = Math.max(previousWatched, reqDTO.watchedSeconds());
        item.put("watchedSeconds", watchedSeconds);
        int minDuration = material.getMinDurationSeconds() == null ? 60 : material.getMinDurationSeconds();
        if (watchedSeconds >= minDuration && !item.has("completedAt")) {
            item.put("completedAt", LocalDateTime.now().toString());
        }
        progressRoot.set(String.valueOf(materialId), item);
        record.setLearningProgressJson(writeProgressRoot(progressRoot));
        if (record.getLearningCompletedAt() != null && !allRequiredMaterialsCompleted(record)) {
            record.setLearningCompletedAt(null);
        }
        sitterTrainingRecordDao.updateById(record);

        SitterProfileDO profile = ensureProfile(userId);
        if (!Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_PASSED)) {
            profile.setVerifyStatus(VERIFY_STATUS_LEARNING);
            sitterProfileDao.updateById(profile);
        }
        return getCurriculum();
    }

    @Transactional
    public void completeLearning() {
        Long userId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(userId);
        requireRealNameVerified(user);
        SitterProfileDO profile = ensureProfile(userId);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        if (!allRequiredMaterialsCompleted(record)) {
            throw new ClientException(BaseErrorCode.TRAINING_LEARNING_INCOMPLETE_ERROR);
        }
        record.setLearningCompletedAt(LocalDateTime.now());
        sitterTrainingRecordDao.updateById(record);
        if (!Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_PASSED)) {
            profile.setVerifyStatus(VERIFY_STATUS_LEARNING);
            sitterProfileDao.updateById(profile);
        }
    }

    @Transactional
    public StartExamRespDTO startExam() {
        Long userId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(userId);
        requireRealNameVerified(user);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        if (record.getLearningCompletedAt() == null || !allRequiredMaterialsCompleted(record)) {
            throw new ClientException(BaseErrorCode.TRAINING_LEARNING_REQUIRED_ERROR);
        }
        List<QuestionBankDO> basics = questionBankDao.selectRandomByType(QUESTION_TYPE_BASIC, BASIC_COUNT);
        List<QuestionBankDO> cores = questionBankDao.selectRandomByType(QUESTION_TYPE_CORE, CORE_COUNT);
        if (basics.size() < BASIC_COUNT || cores.size() < CORE_COUNT) {
            throw new ClientException(BaseErrorCode.TRAINING_QUESTION_LACK_ERROR);
        }
        List<QuestionBankDO> selected = new ArrayList<>(TOTAL_QUESTION_COUNT);
        selected.addAll(basics);
        selected.addAll(cores);
        List<Long> questionIds = selected.stream().map(QuestionBankDO::getQuestionId).toList();
        record.setExamQuestionIdsJson(writeQuestionIds(questionIds));
        record.setExamStartedAt(LocalDateTime.now());
        sitterTrainingRecordDao.updateById(record);

        List<ExamQuestionDTO> questions = selected.stream().map(this::toExamQuestion).toList();
        return new StartExamRespDTO(questions);
    }

    @Transactional
    public SubmitExamRespDTO submitExam(SubmitExamReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.answers() == null || reqDTO.answers().isEmpty()) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        requireCaretakerRole();
        UserDO user = requireUser(userId);
        requireRealNameVerified(user);
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        if (record.getLearningCompletedAt() == null) {
            throw new ClientException(BaseErrorCode.TRAINING_LEARNING_REQUIRED_ERROR);
        }
        List<Long> sessionQuestionIds = readQuestionIds(record.getExamQuestionIdsJson());
        if (sessionQuestionIds.size() != TOTAL_QUESTION_COUNT) {
            throw new ClientException(BaseErrorCode.TRAINING_EXAM_SESSION_ERROR);
        }
        Map<Long, String> answers = reqDTO.answers().stream()
                .filter(a -> a != null && a.questionId() != null)
                .collect(Collectors.toMap(a -> a.questionId(), a -> a.answer(), (a, b) -> a, LinkedHashMap::new));
        if (answers.size() != TOTAL_QUESTION_COUNT || !answers.keySet().equals(new HashSet<>(sessionQuestionIds))) {
            throw new ClientException(BaseErrorCode.TRAINING_EXAM_SESSION_ERROR);
        }
        List<QuestionBankDO> questions = questionBankDao.selectByIds(sessionQuestionIds);
        Map<Long, QuestionBankDO> questionMap = questions.stream()
                .collect(Collectors.toMap(QuestionBankDO::getQuestionId, q -> q, (a, b) -> a));
        int correctCount = 0;
        boolean corePassed = true;
        for (Long questionId : sessionQuestionIds) {
            QuestionBankDO question = questionMap.get(questionId);
            if (question == null) {
                throw new ClientException(BaseErrorCode.TRAINING_EXAM_SESSION_ERROR);
            }
            String provided = answers.get(questionId);
            boolean correct = isAnswerCorrect(provided, question.getCorrectAnswer());
            if (correct) {
                correctCount++;
            } else if (Objects.equals(question.getQuestionType(), QUESTION_TYPE_CORE)) {
                corePassed = false;
            }
        }
        int score = correctCount * SCORE_PER_QUESTION;
        boolean passed = score >= PASS_SCORE && corePassed;
        record.setLastExamScore(score);
        record.setLastExamPassed(passed ? 1 : 0);
        record.setLastExamAt(LocalDateTime.now());
        record.setExamQuestionIdsJson(null);
        record.setExamStartedAt(null);
        if (passed) {
            record.setResetReason(null);
        }
        sitterTrainingRecordDao.updateById(record);

        SitterProfileDO profile = ensureProfile(userId);
        profile.setVerifyStatus(passed ? VERIFY_STATUS_PASSED : VERIFY_STATUS_LEARNING);
        sitterProfileDao.updateById(profile);

        return new SubmitExamRespDTO(score, passed, corePassed);
    }

    @Transactional
    public void resetTraining(Long providerId, String reason) {
        if (providerId == null) {
            return;
        }
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile == null || !Objects.equals(profile.getVerifyStatus(), VERIFY_STATUS_PASSED)) {
            return;
        }
        profile.setVerifyStatus(VERIFY_STATUS_INIT);
        sitterProfileDao.updateById(profile);

        ensureTrainingRecord(providerId);
        sitterTrainingRecordDao.resetProgressForProvider(providerId, reason);
    }

    @Transactional
    public TempCaretakerReadyRespDTO passTrainingTemporarily() {
        Long userId = currentUserId();
        UserDO user = requireUser(userId);
        Integer nextRoleType = resolveReadyRoleType(user.getRoleType());
        if (!Objects.equals(user.getRoleType(), nextRoleType)) {
            user.setRoleType(nextRoleType);
        }
        if (!isRealNameVerified(user)) {
            user.setRealName(buildTempRealName(userId));
            user.setIdCardNo(buildTempIdCardNo(userId));
        }
        userDao.updateById(user);

        LocalDateTime now = LocalDateTime.now();
        SitterTrainingRecordDO record = ensureTrainingRecord(userId);
        if (record.getLearningCompletedAt() == null) {
            record.setLearningCompletedAt(now);
        }
        record.setLastExamScore(100);
        record.setLastExamPassed(1);
        record.setLastExamAt(now);
        record.setResetReason(null);
        record.setExamQuestionIdsJson(null);
        record.setExamStartedAt(null);
        sitterTrainingRecordDao.updateById(record);

        SitterProfileDO profile = ensureProfile(userId);
        profile.setVerifyStatus(VERIFY_STATUS_PASSED);
        profile.setDepositAmount(TEMP_READY_DEPOSIT_AMOUNT);
        if (profile.getCreditScore() == null) {
            profile.setCreditScore(DEFAULT_CREDIT_SCORE);
        }
        if (profile.getIsBanned() == null) {
            profile.setIsBanned(0);
        }
        if (profile.getServiceRadiusKm() == null) {
            profile.setServiceRadiusKm(DEFAULT_SERVICE_RADIUS_KM);
        }
        sitterProfileDao.updateById(profile);

        String token = jwtUtil.generateAccessToken(new UserInfoDTO(
                user.getUserId(),
                user.getPhone(),
                user.getNickname(),
                nextRoleType,
                null));

        return new TempCaretakerReadyRespDTO(
                user.getUserId(),
                nextRoleType,
                UserRoleTypeEnum.getDescByCode(nextRoleType),
                token,
                profile.getVerifyStatus(),
                true,
                TEMP_READY_DEPOSIT_AMOUNT.toPlainString(),
                true);
    }

    private void markLearningStarted(SitterTrainingRecordDO record) {
        if (record.getLearningStartedAt() == null) {
            record.setLearningStartedAt(LocalDateTime.now());
            sitterTrainingRecordDao.updateById(record);
        }
    }

    private TrainingMaterialDO requireRequiredMaterial(Long materialId) {
        TrainingMaterialDO material = trainingMaterialDao.selectByMaterialId(materialId);
        if (material == null || !Objects.equals(material.getIsRequired(), 1)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return material;
    }

    private CurriculumProgress buildCurriculumProgress(SitterTrainingRecordDO record) {
        List<TrainingMaterialDO> materials = trainingMaterialDao.selectRequiredCurriculum();
        ObjectNode progressRoot = readProgressRoot(record.getLearningProgressJson());
        List<TrainingMaterialItemRespDTO> items = new ArrayList<>();
        int completedCount = 0;
        for (TrainingMaterialDO material : materials) {
            ObjectNode item = progressRoot.has(String.valueOf(material.getMaterialId()))
                    ? (ObjectNode) progressRoot.get(String.valueOf(material.getMaterialId()))
                    : objectMapper.createObjectNode();
            int watchedSeconds = item.has("watchedSeconds") ? item.get("watchedSeconds").asInt(0) : 0;
            int minDuration = material.getMinDurationSeconds() == null ? 60 : material.getMinDurationSeconds();
            boolean completed = item.has("completedAt") || watchedSeconds >= minDuration;
            if (completed) {
                completedCount++;
            }
            items.add(new TrainingMaterialItemRespDTO(
                    material.getMaterialId(),
                    material.getTitle(),
                    material.getContent(),
                    material.getMaterialType(),
                    material.getModuleCode(),
                    material.getSortOrder(),
                    minDuration,
                    material.getMediaUrl(),
                    watchedSeconds,
                    completed));
        }
        int requiredCount = materials.size();
        int progressPercent = requiredCount == 0 ? 0 : (completedCount * 100 / requiredCount);
        return new CurriculumProgress(requiredCount, completedCount, progressPercent, items);
    }

    private boolean allRequiredMaterialsCompleted(SitterTrainingRecordDO record) {
        CurriculumProgress progress = buildCurriculumProgress(record);
        return progress.requiredCount() > 0 && progress.completedCount() >= progress.requiredCount();
    }

    private ObjectNode readProgressRoot(String json) {
        if (json == null || json.isBlank()) {
            return objectMapper.createObjectNode();
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            if (node instanceof ObjectNode objectNode) {
                return objectNode;
            }
        } catch (Exception ignored) {
            // fall through
        }
        return objectMapper.createObjectNode();
    }

    private String writeProgressRoot(ObjectNode root) {
        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private String writeQuestionIds(List<Long> questionIds) {
        try {
            return objectMapper.writeValueAsString(questionIds);
        } catch (Exception ex) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private List<Long> readQuestionIds(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private ExamQuestionDTO toExamQuestion(QuestionBankDO question) {
        List<String> options = parseOptions(question.getOptionsJson());
        return new ExamQuestionDTO(question.getQuestionId(), question.getQuestionType(), question.getContent(), options);
    }

    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) {
            return List.of();
        }
        try {
            JsonNode node = objectMapper.readTree(optionsJson);
            JsonNode optionsNode = node.get("options");
            if (optionsNode == null || !optionsNode.isArray()) {
                return List.of();
            }
            List<String> options = new ArrayList<>();
            optionsNode.forEach(item -> options.add(item.asText()));
            return options;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private boolean isAnswerCorrect(String provided, String correct) {
        if (provided == null || correct == null) {
            return false;
        }
        String left = provided.trim();
        String right = correct.trim();
        return left.equals(right) || left.contains(right) || right.contains(left);
    }

    private UserDO requireUser(Long userId) {
        UserDO user = userDao.selectById(userId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return user;
    }

    private void requireCaretakerRole() {
        Integer roleType = UserContext.getRoleType();
        if (!isCaretakerRole(roleType)) {
            throw new ClientException(BaseErrorCode.TRAINING_CARETAKER_REQUIRED_ERROR);
        }
    }

    private boolean isCaretakerRole(Integer roleType) {
        return roleType != null && (roleType == ROLE_CARETAKER || roleType == ROLE_BOTH);
    }

    private void requireRealNameVerified(UserDO user) {
        if (!isRealNameVerified(user)) {
            throw new ClientException(BaseErrorCode.TRAINING_REALNAME_REQUIRED_ERROR);
        }
    }

    private boolean isRealNameVerified(UserDO user) {
        return user.getRealName() != null && !user.getRealName().isBlank()
                && user.getIdCardNo() != null && !user.getIdCardNo().isBlank();
    }

    private Integer resolveReadyRoleType(Integer currentRoleType) {
        if (currentRoleType == null || Objects.equals(currentRoleType, ROLE_OWNER)) {
            return ROLE_BOTH;
        }
        if (Objects.equals(currentRoleType, ROLE_CARETAKER) || Objects.equals(currentRoleType, ROLE_BOTH)) {
            return currentRoleType;
        }
        throw new ClientException(BaseErrorCode.CLIENT_ERROR);
    }

    private String buildTempRealName(Long userId) {
        return "临时认证" + (userId == null ? "" : userId);
    }

    private String buildTempIdCardNo(Long userId) {
        long suffix = userId == null ? 0L : Math.abs(userId % 10000);
        return "11010119900101" + String.format("%04d", suffix);
    }

    private SitterProfileDO ensureProfile(Long providerId) {
        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile == null) {
            profile = new SitterProfileDO();
            profile.setProviderId(providerId);
            profile.setVerifyStatus(VERIFY_STATUS_INIT);
            profile.setDepositAmount(BigDecimal.ZERO);
            profile.setCreditScore(DEFAULT_CREDIT_SCORE);
            profile.setIsBanned(0);
            profile.setServiceRadiusKm(DEFAULT_SERVICE_RADIUS_KM);
            sitterProfileDao.insert(profile);
        }
        return profile;
    }

    private SitterTrainingRecordDO ensureTrainingRecord(Long providerId) {
        SitterTrainingRecordDO record = sitterTrainingRecordDao.selectById(providerId);
        if (record == null) {
            record = new SitterTrainingRecordDO();
            record.setProviderId(providerId);
            sitterTrainingRecordDao.insert(record);
        }
        return record;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private record CurriculumProgress(
            int requiredCount,
            int completedCount,
            int progressPercent,
            List<TrainingMaterialItemRespDTO> materials) {
    }
}
