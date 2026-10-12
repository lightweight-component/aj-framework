package com.ajaxjs.fileupload.magicnumber;

import lombok.RequiredArgsConstructor;

/**
 * Represents a parsed EBML variable-length integer (VINT).
 */
@RequiredArgsConstructor
public class Vint {
    /**
     * The number of bytes consumed by the VINT encoding.
     */
    final int length;

    /**
     * The decoded integer value.
     */
    final long value;
}