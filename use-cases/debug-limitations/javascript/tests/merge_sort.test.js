const { mergeSort } = require('../merge_sort');

describe('mergeSort', () => {
  test('returns an empty array for empty input', () => {
    expect(mergeSort([])).toEqual([]);
  });

  test('returns a copy for a single-element array', () => {
    const input = [5];
    const result = mergeSort(input);

    expect(result).toEqual([5]);
    expect(result).not.toBe(input);
  });

  test('sorts an already sorted array', () => {
    expect(mergeSort([1, 2, 3, 4, 5])).toEqual([1, 2, 3, 4, 5]);
  });

  test('sorts a reverse-sorted array', () => {
    expect(mergeSort([5, 4, 3, 2, 1])).toEqual([1, 2, 3, 4, 5]);
  });

  test('sorts arrays containing duplicate values', () => {
    expect(mergeSort([3, 1, 4, 1, 5, 9, 2, 6])).toEqual([1, 1, 2, 3, 4, 5, 6, 9]);
  });

  test('sorts negative, zero and positive numbers', () => {
    expect(mergeSort([0, -3, 8, -1, 4, -7])).toEqual([-7, -3, -1, 0, 4, 8]);
  });

  test('sorts a large array consistently', () => {
    const input = Array.from({ length: 1000 }, (_, index) => (index * 37) % 1000 - 500);
    const expected = [...input].sort((a, b) => a - b);

    expect(mergeSort(input)).toEqual(expected);
  });

  test('does not mutate the input array', () => {
    const input = [4, 2, 7, 1, 3];
    const original = [...input];

    mergeSort(input);

    expect(input).toEqual(original);
  });

  test('rejects non-array input', () => {
    expect(() => mergeSort('not an array')).toThrow(TypeError);
  });
});
