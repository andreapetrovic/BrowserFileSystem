import { fireEvent, render, screen } from '@testing-library/react';
import ActionBar from './ActionBar';

const renderActionBar = (overrides = {}) => {
  const props = {
    currentFolder: 'guides',
    folderPath: [{ id: 'docs', name: 'Docs' }, { id: 'guides', name: 'Guides' }],
    onNavigateToFolder: jest.fn(),
    onGoBack: jest.fn(),
    newFileName: '',
    setNewFileName: jest.fn(),
    onCreateFile: jest.fn(),
    newFolderName: '',
    setNewFolderName: jest.fn(),
    onCreateFolder: jest.fn(),
    searchQuery: 'read',
    setSearchQuery: jest.fn(),
    suggestions: [{ id: 'file', name: 'README.md', folder: false }],
    onSelectSuggestion: jest.fn(),
    ...overrides
  };
  render(<ActionBar {...props} />);
  return props;
};

test('renders the full path and navigates to selected breadcrumb levels', () => {
  const props = renderActionBar();

  fireEvent.click(screen.getByRole('button', { name: 'Root Folder' }));
  fireEvent.click(screen.getByRole('button', { name: 'Docs' }));
  fireEvent.click(screen.getByRole('button', { name: 'Back' }));

  expect(props.onNavigateToFolder).toHaveBeenNthCalledWith(1, -1);
  expect(props.onNavigateToFolder).toHaveBeenNthCalledWith(2, 0);
  expect(props.onGoBack).toHaveBeenCalledTimes(1);
});

test('shows backend autocomplete suggestions and lets users select one', () => {
  const props = renderActionBar();

  fireEvent.click(screen.getByRole('button', { name: 'README.md' }));

  expect(props.onSelectSuggestion).toHaveBeenCalledWith('README.md');
});
