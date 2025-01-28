class DataLoader
    def __init__(self, dataset, batch_size, shuffle, num_workers, pin_memory):
        self.dataset = dataset
        self.batch_size = batch_size
        self.shuffle = shuffle
        self.num_workers = num_workers
        self.pin_memory = pin_memory

        self.dataloader = DataLoader(
            self.dataset,
            batch_size=self.batch_size,
            shuffle=self.shuffle,
            num_workers=self.num_workers,
            pin_memory=self.pin_memory
        )

    def __iter__(self):
        return iter(self.dataloader)

    def __len__(self):
        return len(self.dataloader)

    def __getitem__(self, idx):
        return self.dataloader[idx]

    def __next__(self):

        return next(self.dataloader)

    def __str__(self):
        return str(self.dataloader)


