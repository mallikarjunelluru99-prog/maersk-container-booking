package com.maersk.container.booking.model;

import com.maersk.container.booking.validation.OneOfInt;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityRequest {
    @NotNull(message = "Container size is required")
    @OneOfInt(value = {20, 40}, message = "Container size must be either 20 or 40")
    private Integer containerSize;

    @NotNull(message = "Container type is required")
    private ContainerType containerType;

    @NotBlank(message = "Origin is required")
    @Size(min = 5, max = 20, message = "Origin length must be between 5 and 20")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Size(min = 5, max = 20, message = "Destination length must be between 5 and 20")
    private String destination;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity must be at most 100")
    private Integer quantity;
}

