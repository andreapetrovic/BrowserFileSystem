import { fireEvent, render, screen } from '@testing-library/react';
import FileItem from './FileItem';

const folder = {
  id: 'docs',
  name: 'Docs',
  folder: true,
  updatedAt: '2026-01-01T10:00:00Z'
};

test('opens a folder when its name is clicked', () => {
  const onOpenFolder = jest.fn();
  render(<FileItem file={folder} onOpenFolder={onOpenFolder} onRename={jest.fn()} onDelete={jest.fn()} />);

  fireEvent.click(screen.getByRole('button', { name: 'Docs' }));

  expect(onOpenFolder).toHaveBeenCalledWith(folder);
});
