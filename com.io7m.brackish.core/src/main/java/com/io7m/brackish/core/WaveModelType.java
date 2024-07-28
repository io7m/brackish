/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */


package com.io7m.brackish.core;

import com.io7m.jranges.RangeCheckException;

/**
 * <p>The wave model.</p>
 *
 * <p>
 * The wave model interface exposes access to an underlying sampled
 * waveform.
 * </p>
 */

public interface WaveModelType
{
  /**
   * @return The number of sample frames in the underlying waveform
   */

  long frameCount();

  /**
   * @return The number of channels in the underlying waveform
   */

  int channelCount();

  /**
   * Retrieve the sample in the {@code channel} at frame {@code frameIndex}.
   *
   * @param channel    The channel
   * @param frameIndex The frame index
   *
   * @return The sample
   *
   * @throws RangeCheckException If {@code frameIndex >= frameCount()} or {@code channel >= channelCount()}
   */

  double sample(
    int channel,
    long frameIndex)
    throws RangeCheckException;

  /**
   * Retrieve the sample in the {@code channel} at frame {@code frameIndex},
   * or return {@code orElse} if the frame or channel is out of range.
   *
   * @param channel    The channel
   * @param frameIndex The frame index
   * @param orElse     The default value to return for invalid frame indices
   *
   * @return The sample
   */

  double sampleOrDefault(
    int channel,
    long frameIndex,
    double orElse
  );

  /**
   * Call {@link #sampleOrDefault(int, long, double)} for the given
   * frame index, and the frame index that follows it, and linearly
   * interpolate between the two values based on the fractional value
   * of {@code frameIndex}.
   *
   * @param channel    The channel index
   * @param frameIndex The frame index
   *
   * @return The interpolated sample
   */

  default double sampleLerp(
    final int channel,
    final double frameIndex)
  {
    final double i0 = Math.floor(frameIndex);
    final double i1 = i0 + 1.0;
    final double factor = frameIndex - i0;

    final var x0 =
      this.sampleOrDefault(channel, (long) i0, 0.0);
    final var x1 =
      this.sampleOrDefault(channel, (long) i1, 0.0);

    return (x0 * (1 - factor)) + (x1 * factor);
  }
}
