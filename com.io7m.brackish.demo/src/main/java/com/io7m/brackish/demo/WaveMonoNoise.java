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


package com.io7m.brackish.demo;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A demo waveform consisting of random noise.
 */

public final class WaveMonoNoise implements WaveDemoModelType
{
  private final double[] data;
  private final SecureRandom rng;

  /**
   * A demo waveform consisting of random noise.
   *
   * @param size The number of samples
   */

  public WaveMonoNoise(
    final int size)
  {
    this.data = new double[size];

    try {
      this.rng = SecureRandom.getInstanceStrong();
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void update()
  {
    for (int index = 0; index < this.data.length; ++index) {
      this.data[index] = this.rng.nextDouble() - 0.5;
    }
  }

  @Override
  public String toString()
  {
    return "WaveMonoNoise";
  }

  @Override
  public long frameCount()
  {
    return Integer.toUnsignedLong(this.data.length);
  }

  @Override
  public int channelCount()
  {
    return 1;
  }

  @Override
  public double sample(
    final int channel,
    final long frameIndex)
  {
    return this.data[Math.toIntExact(frameIndex)];
  }

  @Override
  public double sampleOrDefault(
    final int channel,
    final long frameIndex,
    final double orElse)
  {
    try {
      return this.sample(channel, frameIndex);
    } catch (final Exception e) {
      return orElse;
    }
  }
}
