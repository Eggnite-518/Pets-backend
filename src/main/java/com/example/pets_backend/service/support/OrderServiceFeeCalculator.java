package com.example.pets_backend.service.support;

import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dto.req.OrderRequirementTagsReqDTO;
import com.example.pets_backend.dto.req.PetProfileTagsReqDTO;
import com.example.pets_backend.dto.resp.OrderServiceFeeQuoteRespDTO;
import com.example.pets_backend.enums.OrderRequirementTagEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceFeeCalculator {

    private static final int PET_TYPE_DOG = 2;
    private static final int PET_TYPE_EXOTIC = 3;

    private static final BigDecimal BASE_VISIT_FEE = BigDecimal.valueOf(30);
    private static final BigDecimal FIRST_TIER_CITY_SURCHARGE = BigDecimal.TEN;
    private static final BigDecimal WEEKEND_SURCHARGE = BigDecimal.TEN;
    private static final BigDecimal EXTRA_PET_FEE = BigDecimal.valueOf(5);
    private static final BigDecimal LARGE_DOG_FEE = BigDecimal.valueOf(5);
    private static final BigDecimal EXOTIC_PET_FEE = BigDecimal.TEN;
    private static final BigDecimal PLAY_COMPANION_FEE = BigDecimal.valueOf(5);
    private static final BigDecimal CLEANING_SERVICE_FEE = BigDecimal.TEN;
    private static final BigDecimal DISTANCE_SURCHARGE = BigDecimal.ZERO;
    private static final BigDecimal LARGE_DOG_MIN_WEIGHT_KG = BigDecimal.valueOf(25);

    private static final String CURRENCY_CNY = "CNY";

    private final OrderRequirementTagService orderRequirementTagService;
    private final PetProfileTagService petProfileTagService;

    public OrderServiceFeeQuoteRespDTO quote(
            UserAddressDO address,
            LocalDate serviceDate,
            List<PetArchiveDO> pets,
            OrderRequirementTagsReqDTO requirementTags) {
        List<PetArchiveDO> safePets = pets == null ? List.of() : pets;
        int petCount = safePets.size();
        int extraPetCount = Math.max(petCount - 1, 0);
        int largeDogCount = countLargeDogs(safePets);
        int exoticPetCount = countExoticPets(safePets);
        boolean needPlayCompanion = orderRequirementTagService.hasPlayCompanion(requirementTags);
        boolean needCleaning = orderRequirementTagService.hasCleaningService(requirementTags);

        BigDecimal baseVisitFee = calculateBaseVisitFee(address, serviceDate);
        BigDecimal extraPetAmount = EXTRA_PET_FEE.multiply(BigDecimal.valueOf(extraPetCount));
        BigDecimal largeDogAmount = LARGE_DOG_FEE.multiply(BigDecimal.valueOf(largeDogCount));
        BigDecimal exoticPetAmount = EXOTIC_PET_FEE.multiply(BigDecimal.valueOf(exoticPetCount));
        BigDecimal playCompanionAmount = needPlayCompanion ? PLAY_COMPANION_FEE : BigDecimal.ZERO;
        BigDecimal cleaningAmount = needCleaning ? CLEANING_SERVICE_FEE : BigDecimal.ZERO;
        BigDecimal specialTagAmount = playCompanionAmount.add(cleaningAmount);

        BigDecimal totalAmount = baseVisitFee
                .add(extraPetAmount)
                .add(largeDogAmount)
                .add(exoticPetAmount)
                .add(specialTagAmount)
                .add(DISTANCE_SURCHARGE);

        List<OrderServiceFeeQuoteRespDTO.PriceItemRespDTO> priceItems = new ArrayList<>();
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "基础上门费",
                formatAmount(baseVisitFee),
                1,
                buildBaseVisitFeeRemark(address, serviceDate)));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "多宠附加费",
                formatAmount(EXTRA_PET_FEE),
                extraPetCount,
                "5 元/只，首只宠物不收取"));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "大型犬附加费",
                formatAmount(LARGE_DOG_FEE),
                largeDogCount,
                "5 元/只，体重≥25kg 的犬只"));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "异宠附加费",
                formatAmount(EXOTIC_PET_FEE),
                exoticPetCount,
                "10 元/只，异宠类型宠物"));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "陪玩增值费",
                formatAmount(PLAY_COMPANION_FEE),
                needPlayCompanion ? 1 : 0,
                OrderRequirementTagEnum.NEED_PLAY_COMPANION.getDesc() + " +5 元"));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "清洁增值费",
                formatAmount(CLEANING_SERVICE_FEE),
                needCleaning ? 1 : 0,
                OrderRequirementTagEnum.NEED_CLEANING.getDesc() + " +10 元"));
        priceItems.add(new OrderServiceFeeQuoteRespDTO.PriceItemRespDTO(
                "距离附加费",
                formatAmount(DISTANCE_SURCHARGE),
                1,
                "当前地址暂无距离附加费"));

        return new OrderServiceFeeQuoteRespDTO(CURRENCY_CNY, formatAmount(totalAmount), priceItems);
    }

    private BigDecimal calculateBaseVisitFee(UserAddressDO address, LocalDate serviceDate) {
        BigDecimal fee = BASE_VISIT_FEE;
        if (FirstTierCityMatcher.matches(address)) {
            fee = fee.add(FIRST_TIER_CITY_SURCHARGE);
        }
        if (isWeekend(serviceDate)) {
            fee = fee.add(WEEKEND_SURCHARGE);
        }
        return fee;
    }

    private String buildBaseVisitFeeRemark(UserAddressDO address, LocalDate serviceDate) {
        StringBuilder remark = new StringBuilder("固定 30 元/次");
        if (FirstTierCityMatcher.matches(address)) {
            remark.append("，一线城市 +10 元");
        }
        if (isWeekend(serviceDate)) {
            remark.append("，周末 +10 元");
        }
        return remark.toString();
    }

    private boolean isWeekend(LocalDate serviceDate) {
        if (serviceDate == null) {
            return false;
        }
        DayOfWeek dayOfWeek = serviceDate.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private int countLargeDogs(List<PetArchiveDO> pets) {
        int count = 0;
        for (PetArchiveDO pet : pets) {
            if (isLargeDog(pet)) {
                count++;
            }
        }
        return count;
    }

    private int countExoticPets(List<PetArchiveDO> pets) {
        int count = 0;
        for (PetArchiveDO pet : pets) {
            if (pet != null && Objects.equals(pet.getPetType(), PET_TYPE_EXOTIC)) {
                count++;
            }
        }
        return count;
    }

    private boolean isLargeDog(PetArchiveDO pet) {
        if (pet == null || !Objects.equals(pet.getPetType(), PET_TYPE_DOG)) {
            return false;
        }
        PetProfileTagsReqDTO profileTags = petProfileTagService.parse(pet.getProfileTagsJson());
        if (profileTags == null || profileTags.weightKg() == null) {
            return false;
        }
        return profileTags.weightKg().compareTo(LARGE_DOG_MIN_WEIGHT_KG) >= 0;
    }

    private String formatAmount(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
